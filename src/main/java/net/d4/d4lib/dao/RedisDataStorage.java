package net.d4.d4lib.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisDataStorage {
//	private static Logger logger = LoggerFactory.getLogger(RedisDataStorage.class);
	/**
	 * redis连接池
	 */
	private JedisPool pool;

	public void init(String address,int port) {
		pool = new JedisPool(address, port);
	}

	public boolean exists(String key) {
		Jedis jedis = getJedis();
		try {
			boolean rs = jedis.exists(key);
			return rs;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 返回VALUE
	 * 
	 * @param primary
	 * @param key
	 * @return
	 */
	public byte[] get(String key, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			byte[] k = key.getBytes();
			if (expireTime != 0)
				p.expire(k, expireTime);
			Response<byte[]> rs = p.get(k);
			p.sync();
			return rs.get();
		} finally {
			returnResource(jedis);
		}
	}

	public String get(String key) {
		Jedis jedis = getJedis();
		try {
			String rs = jedis.get(key);
			return rs;
		} finally {
			returnResource(jedis);
		}
	}

	public byte[] delAndGet(String key) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			byte[] k = key.getBytes();
			Response<byte[]> rs = p.get(k);
			p.del(k);
			p.sync();
			return rs.get();
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 删除
	 * 
	 * @param key
	 * @return
	 */
	public void del(String key) {
		Jedis jedis = getJedis();
		try {
			jedis.del(key);
		} finally {
			returnResource(jedis);
		}
	}

	public void set(String key, String value, int expireTime) {
		Jedis jedis = getJedis();
		try {
			if (expireTime != 0) {
				jedis.setex(key, expireTime, value);
			} else {
				jedis.set(key, value);
			}
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 将kv以字符串的形式存入redis
	 * 
	 * @param key
	 * @param value
	 * @param expireTime
	 */
	public void set(Map<String, String> map, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			for (Entry<String, String> key : map.entrySet()) {
				p.set(key.getKey(), key.getValue());
				if (expireTime != 0)
					p.expire(key.getKey(), expireTime);
			}

			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	public void setAt(Map<String, String> map, long millisecondsTimestamp) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			for (Entry<String, String> key : map.entrySet()) {
				p.set(key.getKey(), key.getValue());
				if (millisecondsTimestamp != 0)
					p.pexpireAt(key.getKey(), millisecondsTimestamp);
			}
			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 更新时间
	 * 
	 * @param map
	 * @param expireTime
	 */
	public void expire(String[] keys, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			for (String key : keys) {
				p.expire(key, expireTime);
			}
			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 根据字符串key获取其value
	 * 
	 * @param key
	 * @return
	 */
	public List<Object> getstring(String[] key) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			for (int i = 0; i < key.length; i++) {
				p.get(key[i]);
			}
			List<Object> list = p.syncAndReturnAll();

			return list;
		} finally {
			returnResource(jedis);
		}
	}

	/**
	 * 实现value 的自增自减
	 * 
	 * @param key
	 * @param cost
	 * @param expireTime
	 * @return
	 */
	public void add(Map<String, Long> map, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();

			for (Entry<String, Long> key : map.entrySet()) {
				if (key.getKey().startsWith("Offset_")) {
					p.set(key.getKey(), key.getValue() + "");
				} else {
					p.incrBy(key.getKey(), key.getValue());
				}
				p.expire(key.getKey(), expireTime);
			}
			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	public void rpush(String key, byte[] value, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			byte[] k = key.getBytes();
			p.rpush(k, value);
			p.expire(k, expireTime);
		} finally {
			returnResource(jedis);
		}
	}

	public void batchPop(String key, byte[] value, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			byte[] k = key.getBytes();
			p.lpop(k);
			p.rpush(k, value);
			p.expire(k, expireTime);
		} finally {
			returnResource(jedis);
		}
	}

	public List<byte[]> lrange(String key, int starte, int ende, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			byte[] k = key.getBytes();
			p.expire(k, expireTime);
			Response<List<byte[]>> rs = p.lrange(key.getBytes(), starte, ende);
			p.sync();
			return rs.get();
		} finally {
			returnResource(jedis);
		}
	}

	public void sadd(String key, String value, int expireTime) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			p.sadd(key, value);
			p.expire(key, expireTime);
			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	public Set<String> smembers(String key) {
		Jedis jedis = getJedis();
		try {
			Set<String> sets = jedis.smembers(key);
			return sets;
		} finally {
			returnResource(jedis);
		}
	}

	public Jedis getJedis() {
		return pool.getResource();
	}

	public void returnResource(Jedis jedis) {
		if (jedis != null)
			jedis.close();
	}

	/**
	 * 根据key批量获取kv库中的list
	 */
	public List<Object> getLists(String[] keys) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			for (int i = 0; i < keys.length; i++) {
				p.lrange(keys[i], 0, -1);
			}
			List<Object> list = p.syncAndReturnAll();
			return list;
		} finally {
			returnResource(jedis);
		}
	}

	public void incr(String key) {
		Jedis jedis = getJedis();
		try {
			jedis.incr(key);
		} finally {
			returnResource(jedis);
		}
	}

	public void incr(String key, int expire) {
		Jedis jedis = getJedis();
		try {
			Pipeline p = jedis.pipelined();
			p.incr(key);
			p.expire(key, expire);
			p.sync();
		} finally {
			returnResource(jedis);
		}
	}

	public long ttl(String key) {
		Jedis jedis = getJedis();
		try {
			return jedis.ttl(key);
		} finally {
			returnResource(jedis);
		}
	}

}
