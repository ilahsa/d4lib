package net.d4.d4lib.dao;
//
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.Field;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.dsp.storm.bean.CampaignBean;
//import com.dsp.storm.bean.DBBean;
//import com.dsp.storm.bean.bidwon.BidwonCacheBean;
//import com.dsp.storm.bean.bidwon.BidwonOperationLog;
//import com.dsp.storm.bean.winrate.OperationLog;
//import com.dsp.storm.business.bidwon.impl.BidwonStrategy;
//import com.dsp.storm.business.validate.CampaignBeanValidator;
//import com.dsp.storm.common.DateUtil;
//import com.dsp.storm.common.WRConst;
//
//public class DBC3P0ManagerLeo {
//
//	private static final Logger logger = LoggerFactory.getLogger(DBC3P0ManagerLeo.class);
//
//	// campaign的状态标志值
//	private static final int invaildTag = 2;
//	// 每次从数据库查询的条数
//	private final int each = 10000;
//
//	private boolean hasMemory;
//
//	public ConcurrentHashMap<Integer, CampaignBean> beans = new ConcurrentHashMap<>();
//
//	private DBC3P0ManagerLeo() {
//		super();
//		// init(true);
//	}
//
//	private static class DBManagerHolder {
//		private static final DBC3P0ManagerLeo instance = new DBC3P0ManagerLeo();
//	}
//
//	public static DBC3P0ManagerLeo getInstance() {
//		return DBManagerHolder.instance;
//	}
//
//	public void init() {
//		init(false);
//	}
//
//	public void init(boolean initCache) {
//		this.hasMemory = initCache;
//
//		/* 每次构建Map前记得清除 */
//		beans.clear();
//		initCampaignIdList();
//		initCampaignConfigItems(null);
//
//		Set<Integer> set = new HashSet<>();
//		Iterator<Entry<Integer, CampaignBean>> it = beans.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<Integer, CampaignBean> entry = it.next();
//			CampaignBean bean = entry.getValue();
//			if (CampaignBeanValidator.getInstance().validate(bean)) {
//				if (initCache) {
//					BidwonCacheBean cacheBean = new BidwonCacheBean(bean);
//					BidwonStrategy.cacheMap.putIfAbsent(entry.getKey(), cacheBean);
//					set.add(bean.getCid());
//				}
//			} else {
//				logger.info("Campaign : " + entry.getKey() + " is not verfied by.");
//				it.remove();
//			}
//		}
//
//		String cidStr = getSetStr(set);
//		try {
//			List<BidwonOperationLog> historyList = getHistoryLogsLeo(cidStr);
//			String history = "";
//			if (null != historyList && historyList.size() > 0) {
//				for (BidwonOperationLog operationLog : historyList) {
//					BidwonCacheBean bean = BidwonStrategy.cacheMap.get(operationLog.getCid());
//					if (null != bean) {
//						history += bean.getCid() + ",";
//						bean.loadFromLog(operationLog);
//					}
//				}
//			}
//			logger.info("DB get Campaigns : " + beans.size() + ", ids : " + cidStr + ", history: " + history);
//		} catch (Exception e) {
//			logger.error("getHistoryLogs error:", e);
//			e.printStackTrace();
//		}
//		logger.info("DB init end.");
//	}
//
//	public synchronized void reload(Set<Integer> set) {
//		String cidsStr = getSetStr(set);
//
//		try {
//			String sql = "SELECT cam.ID campid,cam.BannerPrice BannerPrice,cam.totalBudget FROM Campaigns cam inner join ConfigItems ci on cam.id = ci.CampaignID "
//					//
//					+ "where cam.Status = " + invaildTag
//					//
//					+ " and ci.`key` = \"" + WRConst.OPEN_DYNAMIC_BID + "\" "
//					//
//					+ " and ci.`value` = \"true\" "
//					// 更新的Campaign list
//					+ "and cam.ID in (" + cidsStr + ");";
//			logger.info(sql);
//
//			Set<Integer> updateSet = new HashSet<>();
//			DBBean dbbean = JDBCUtils.query(sql);
//			ResultSet rSet = dbbean.getRs();
//			while (rSet.next()) {
//				int campaignID = getCampaignFromRS(rSet);
//				updateSet.add(campaignID);
//			}
//			JDBCUtils.closeDB(dbbean);
//
//			for (Integer cid : set) {
//				if (!updateSet.contains(cid)) {
//					if (hasMemory) {
//						// 清楚内存BidwonCacheBean
//						BidwonStrategy.removeCache(cid);
//					}
//				}
//			}
//			cidsStr = getSetStr(updateSet);
//			logger.info("DB_reload_Campaigns : " + cidsStr);
//
//			if (updateSet.size() > 0) {
//				initCampaignConfigItems(cidsStr);
//
//				if (hasMemory) {
//					// 重建内存BidwonCacheBean
//					for (Integer cid : updateSet) {
//						CampaignBean campaign = beans.get(cid);
//						if (CampaignBeanValidator.getInstance().validate(campaign)) {
//							BidwonCacheBean cacheBean = BidwonStrategy.cacheMap.get(cid);
//							if (null != cacheBean) {
//								cacheBean.updateCampaign(campaign);
//							} else {
//								BidwonCacheBean newBean = new BidwonCacheBean(campaign);
//								BidwonStrategy.cacheMap.put(cid, newBean);
//							}
//						} else {
//							logger.info("Reload_Campaign : " + cid + " is not verfied by.");
//							BidwonStrategy.removeCache(cid);
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Reload Campaign error:", e);
//			throw new RuntimeException(e);
//		}
//	}
//
//	public int getCampaignFromRS(ResultSet rSet) throws SQLException {
//		int campaignID = rSet.getInt("campid");
//
//		beans.putIfAbsent(campaignID, new CampaignBean());
//		// 构建全量campaign
//		CampaignBean bean = beans.get(campaignID);
//		// camp信息
//		double bannerPrice = rSet.getDouble("BannerPrice");
//		double totalBudget = rSet.getDouble("totalBudget");
//
//		bean.setCid(campaignID);
//		bean.setBannerPrice(bannerPrice * WRConst.MONEY_PRECISION);
//		bean.setTotalBudget(totalBudget);
//		return campaignID;
//	}
//
//	public String getSetStr(Set<Integer> set) {
//		StringBuilder sBuilder = new StringBuilder();
//		int size = set.size();
//		int index = 0;
//		for (Integer cid : set) {
//			sBuilder.append(cid);
//			if (index != size - 1) {
//				sBuilder.append(",");
//			}
//			index++;
//		}
//		String cidsStr = sBuilder.toString();
//		return cidsStr;
//	}
//
//	/**
//	 * 初始化全部可用的campaignid的list
//	 */
//	private void initCampaignIdList() {
//		// 初始化list
//		DBBean dbbean = null;
//		try {
//			int start = 0;
//			// 每次从数据库查询出的条数
//			int count = 0;
//			do {
//				start += count;
//				count = 0;
//				String sql = "SELECT cam.ID campid,cam.BannerPrice BannerPrice,cam.totalBudget FROM Campaigns cam where cam.Status = " + invaildTag + " order by cam.ID limit " + start + "," + each;
//				logger.info(sql);
//
//				dbbean = JDBCUtils.query(sql);
//				ResultSet rSet = dbbean.getRs();
//				while (rSet.next()) {
//					getCampaignFromRS(rSet);
//					count++;
//				}
//				logger.info("Start index:" + start + ", get campaigns : " + count);
//			} while (count > 0);
//		} catch (Exception e) {
//			logger.error("initCampaignIdList() 查询所有的可用的campaign的id异常", e);
//			throw new RuntimeException(e);
//		} finally {
//			if (null != dbbean) {
//				JDBCUtils.closeDB(dbbean);
//			}
//		}
//	}
//
//	private List<BidwonOperationLog> getHistoryLogsLeo(String cidStr) {
//		// 初始化list
//		DBBean dbbean = null;
//		try {
//			List<BidwonOperationLog> list = new ArrayList<>();
//			StringBuilder sBuilder = new StringBuilder();
//			sBuilder.append("select campaign_id,diff_wins,diff_budget,update_time,dynamic_bid from (select * from dynamic_bid_log where 1=1");
//			if (StringUtils.isNotBlank(cidStr)) {
//				sBuilder.append(" and campaign_id in (" + cidStr + ") ");
//			}
//			sBuilder.append(" order by id desc limit 1000) ttttt group by campaign_id;");
//
//			String sql = sBuilder.toString();
//			logger.info(sql);
//			dbbean = JDBCUtils.query(sql);
//			ResultSet rSet = dbbean.getRs();
//			// 从数据库查询出的条数
//			int count = 0;
//			while (rSet.next()) {
//				BidwonOperationLog operationLog = new BidwonOperationLog();
//				int campaignID = rSet.getInt("campaign_id");
//				int diffwins = rSet.getInt("diff_wins");
//				double diffbudget = rSet.getDouble("diff_budget");
//				Timestamp updateTime = rSet.getTimestamp("update_time");
//				double dynamicBid = rSet.getDouble("dynamic_bid") * WRConst.MONEY_PRECISION;
//
//				operationLog.setCid(campaignID);
//				operationLog.setDiffWin(diffwins);
//				operationLog.setDiffBudget(diffbudget);
//				operationLog.setUpdateTime(updateTime);
//				operationLog.setDynamicBid(dynamicBid);
//
//				list.add(operationLog);
//				count++;
//			}
//			logger.info(cidStr + ": get BidwonOperationLogs : " + count);
//			return list;
//		} catch (Exception e) {
//			logger.error("initCampaignIdList() 查询所有的可用的campaign的id异常", e);
//			throw new RuntimeException(e);
//		} finally {
//			if (null != dbbean) {
//				JDBCUtils.closeDB(dbbean);
//			}
//		}
//	}
//
//	private void initCampaignConfigItems(String cidsStr) {
//		long optMapTime = System.currentTimeMillis();
//		try {
//			int start = 0;
//			// 每次从数据库查询出的条数
//			int count = 0;
//			int total = 0;
//			do {
//
//				String sql = "SELECT ConfigItems.`Key`, ConfigItems.`Value`, ConfigItems.CampaignID FROM ConfigItems LEFT OUTER JOIN Campaigns ON ConfigItems.CampaignID = Campaigns.ID "
//						//
//						+ "WHERE Campaigns.`Status` = " + invaildTag
//						//
//						+ " and ConfigItems.`Key` " + "in (\""
//						// dynamicBidPrice
//						+ WRConst.DYNAMIC_BID_PRICE + "\",\""
//						// openDynamicBid
//						+ WRConst.OPEN_DYNAMIC_BID + "\",\""
//						// maxWin
//						+ WRConst.MAX_WIN + "\",\""
//						// step
//						+ WRConst.STEP + "\",\""
//						// maxBid
//						+ WRConst.MAX_BID + "\",\""
//						// accumulateWin
//						+ WRConst.ACCUMULATE_WIN + "\",\""
//						// + WRConst.EXPECT_WIN_RATE + "\",\""
//						// budgetWeight
//						+ WRConst.BUDGET_WEIGHT + "\") ";
//
//				if (StringUtils.isNotBlank(cidsStr)) {
//					sql += "and Campaigns.ID in (" + cidsStr + ") ";
//				}
//				sql += " ORDER BY ConfigItems.Seq ASC, ConfigItems.CampaignID ASC limit " + start + "," + each + ";";
//				logger.info("config item sql :" + sql);
//
//				DBBean dbbean = JDBCUtils.query(sql);
//				ResultSet rSet = dbbean.getRs();
//				count = rSet.getFetchSize();
//				start = start + each;
//				while (rSet.next()) {
//					String key = rSet.getString("Key");
//					String value = rSet.getString("Value");
//					if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
//						continue;
//					}
//					int campaignID = rSet.getInt("CampaignID");
//					CampaignBean bean = beans.get(campaignID);
//					Field field = null;
//					try {
//						field = CampaignBean.class.getDeclaredField(key);
//					} catch (NoSuchFieldException e) {
//						logger.error("not find " + key);
//					}
//					if (field != null) {
//						PropertyDescriptor proDes = new PropertyDescriptor(key, CampaignBean.class);
//						Object args = null;
//						if (field.getType().equals(int.class) || field.getType().equals(Integer.class))
//							args = Integer.valueOf(value);
//						else if (field.getType().equals(String.class))
//							args = value;
//						else if (field.getType().equals(double.class) || field.getType().equals(Double.class))
//							args = Double.valueOf(value);
//						else if (field.getType().equals(float.class) || field.getType().equals(Float.class))
//							args = Float.valueOf(value);
//						else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))
//							args = Boolean.valueOf(value);
//						else
//							throw new RuntimeException("not support type.");
//
//						proDes.getWriteMethod().invoke(bean, args);
//					} else {
//						logger.info("------CampaignID" + campaignID + "未包含配置项" + key + "------");
//					}
//				}
//
//				if (logger.isInfoEnabled()) {
//					total += count;
//					logger.info("初始化Options数据数量:" + total);
//				}
//				JDBCUtils.closeDB(dbbean);
//			} while (count > 0);
//		} catch (Exception e) {
//			logger.error("initCampaignConfigItems 查询所有的可用的campaign的ConfigItems异常", e);
//			throw new RuntimeException(e);
//		}
//
//		long optMapTimecost = (System.currentTimeMillis() - optMapTime) / 1000;
//		logger.info("optMap初始化完毕花费----" + optMapTimecost + "秒");
//	}
//
//	public boolean updateCampaign(double bannerPrice, int cid) {
//		String updatasql = "UPDATE Campaigns SET BannerPrice=? WHERE ID=?";
//
//		try {
//			JDBCUtils.saveOrUpdate(updatasql, bannerPrice, cid);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("error: ", e);
//			return false;
//		}
//		return true;
//	}
//
//	public boolean savaLog(OperationLog operationLog) {
//		CampaignBean cBean = operationLog.getCampaignBean();
//		double adjustedBannerPrice = operationLog.getAdjustPrice() / WRConst.MONEY_PRECISION;
//		double bannerPrice = cBean.getBannerPrice() / WRConst.MONEY_PRECISION;
//		String sql = String.format("insert into price_adjust_log(campaign_id,banner_price,max_bid,step,expect_winrate," + "bids,wins,adjust_price,status,failure_cause,update_time) " + "values(%d,%f,%f,%f,%f,%d,%d,%f,%d,\"%s\",\"%s\");", cBean.getCid(), bannerPrice,
//				cBean.getMaxBid(), cBean.getStep(), cBean.getExpectWinRate(), operationLog.getBids(), operationLog.getWins(), adjustedBannerPrice, operationLog.getStatus(), operationLog.getFailureCause(), DateUtil.getDateStr(Calendar.getInstance().getTime()));
//
//		try {
//			JDBCUtils.saveOrUpdate(sql);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("error: ", e);
//			return false;
//		}
//		return true;
//	}
//
//	public boolean savaBidwonLog(BidwonOperationLog log) {
//		CampaignBean c = beans.get(log.getCid());
//		String sql = String.format("insert into dynamic_bid_log(campaign_id,status,message,dynamic_bid,last_bid,max_bid,wins,period_wins,diff_wins,max_win,cost,period_budget,diff_budget,budget,"
//				//
//				+ "total_budget,total_max_win,total_max_bid,budget_weight,step,update_time) "
//				//
//				+ " values(%d,%d,\"%s\",%f,%f,%f,%d,%d,%d,%d,%f,%f,%f,%f,%f,%d,%f,\"%s\",%f,\"%s\");"
//				// BidwonOperationLog
//				, log.getCid(), log.getStatus(), log.getFailureCause()
//				//
//				, log.getDynamicBid() / WRConst.MONEY_PRECISION, log.getLastBid() / WRConst.MONEY_PRECISION, log.getMaxBid() / WRConst.MONEY_PRECISION
//				//
//				, log.getWins(), log.getPeriodWin(), log.getDiffWin(), log.getMaxWin()
//				//
//				, log.getCost() / WRConst.MONEY_PRECISION, log.getPeriodBudget() / WRConst.MONEY_PRECISION, log.getDiffBudget() / WRConst.MONEY_PRECISION, log.getBudget() / WRConst.MONEY_PRECISION
//				// CampaignBean
//				, c.getTotalBudget() / WRConst.MONEY_PRECISION, c.getMaxWin(), c.getMaxBid() / WRConst.MONEY_PRECISION
//				//
//				, c.getBudgetWeight(), c.getStep() / WRConst.MONEY_PRECISION
//				// timestamp
//				, DateUtil.getDateStr(Calendar.getInstance().getTime()));
//
//		try {
//			JDBCUtils.saveOrUpdate(sql);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("error: ", e);
//			return false;
//		}
//		return true;
//	}
//
//	public Map<Integer, Integer> getCampaignWins(Set<Integer> set) {
//		Map<Integer, Integer> rmap = new HashMap<Integer, Integer>();
//
//		DBBean dbbean = null;
//		try {
//			Calendar calendar = Calendar.getInstance();
//			SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH");
//			String endStr = format.format(calendar.getTime());
//			calendar.add(Calendar.HOUR_OF_DAY, -1);
//			String beginStr = format.format(calendar.getTime());
//
//			StringBuilder sBuilder = new StringBuilder();
//			sBuilder.append("select campaign_id,sum(wins) as swins from dynamic_bid_log where update_time between");
//			sBuilder.append(" \"" + beginStr + "\" and \"" + endStr + "\"");
//			if (null != set && set.size() > 0) {
//				String cidStr = getSetStr(set);
//				sBuilder.append(" and campaign_id in (" + cidStr + ")");
//			}
//			sBuilder.append(" group by campaign_id;");
//
//			String sql = sBuilder.toString();
//			logger.info("getCampaignWins_sql" + sql);
//			dbbean = JDBCUtils.query(sql);
//			ResultSet rSet = dbbean.getRs();
//			// 从数据库查询出的条数
//			while (rSet.next()) {
//				int campaignID = rSet.getInt("campaign_id");
//				int wins = rSet.getInt("swins");
//				rmap.put(campaignID, wins);
//			}
//		} catch (Exception e) {
//			logger.error("getCampaignWins() 查询campaign的小时历史", e);
//			throw new RuntimeException(e);
//		} finally {
//			if (null != dbbean) {
//				JDBCUtils.closeDB(dbbean);
//			}
//		}
//
//		return rmap;
//	}
//
//	public static void main(String[] args) {
//		// DBC3P0Manager.getInstance().initCampaignConfigItems(null);
//
//		// DBC3P0Manager.getInstance().getHistoryLogs("22");
//
//		// CampaignBean campaign = DBC3P0Manager.getInstance().beans.get(8);
//		// BidwonCacheBean bean = BidwonStrategy.cacheMap.get(8);
//		//
//		// BidwonOperationLog log = new BidwonOperationLog();
//		// log.setCid(8);
//		// log.setStatus(1);
//		// log.setDynamicBid(bean.getPeriodBid());
//		// log.setLastBid(100000);
//		// log.setMaxBid(bean.getPeriodMaxBid());
//		// log.setWins(11);
//		// log.setPeriodWin(11);
//		// log.setDiffWin(11);
//		// log.setMaxWin(bean.getPeriodMaxWins());
//		// log.setCost(100000000);
//		// log.setPeriodBudget(1111111d);
//		// log.setDiffBudget(1111111d);
//		// log.setBudget(bean.getPeriodBudget());
//		//
//		// DBC3P0Manager.getInstance().savaBidwonLog(log);
//
//		// OperationLog log = new OperationLog();
//		// WRCampaignBean cBean = new WRCampaignBean();
//		// WRRedisBean rBean = new WRRedisBean();
//		// log.setCampaignBean(cBean);
//		// log.setRedisBean(rBean);
//		// DBC3P0Manager.getInstance().savaLog(log);
//		// DBC3P0Manager.getInstance().init();
//		// DBC3P0Manager.getInstance().init(true);
//		// DBC3P0Manager.getInstance().initCampaignIdList();
//
//		Set<Integer> set = new HashSet<>();
//		set.add(192);
//		set.add(75);
//		set.add(255);
//		set.add(165);
//		set.add(201);
//
//		DBC3P0ManagerLeo.getInstance().getCampaignWins(set);
//
//		// String cidsStr = null;
//		// // String cidsStr = getInstance().getSetStr(set);
//		//
//		// int start = 0;
//		// int each = 1000;
//		// DBC3P0Manager.getInstance().reload(set);
//
//		// String sql = "SELECT cam.ID campid,cam.BannerPrice BannerPrice,cam.totalBudget FROM Campaigns cam inner join ConfigItems ci on cam.id = ci.CampaignID "
//		// //
//		// + "where cam.Status = " + invaildTag
//		// //
//		// + " and ci.`key` = \"" + WRConst.OPEN_DYNAMIC_BID + "\" "
//		// //
//		// + " and ci.`value` = \"true\" "
//		// // 更新的Campaign list
//		// + "and cam.ID in (" + cidsStr + ");";
//
//		// String sql = "SELECT ConfigItems.`Key`, ConfigItems.`Value`, ConfigItems.CampaignID FROM ConfigItems LEFT OUTER JOIN Campaigns ON ConfigItems.CampaignID = Campaigns.ID "
//		// //
//		// + "WHERE Campaigns.`Status` = " + invaildTag
//		// //
//		// + " and ConfigItems.`Key` " + "in (\""
//		// // dynamicBidPrice
//		// + WRConst.DYNAMIC_BID_PRICE + "\",\""
//		// // openDynamicBid
//		// + WRConst.OPEN_DYNAMIC_BID + "\",\""
//		// // maxWin
//		// + WRConst.MAX_WIN + "\",\""
//		// // step
//		// + WRConst.STEP + "\",\""
//		// // maxBid
//		// + WRConst.MAX_BID + "\",\""
//		// // + WRConst.EXPECT_WIN_RATE + "\",\""
//		// // budgetWeight
//		// + WRConst.BUDGET_WEIGHT + "\") ";
//		//
//		// if (StringUtils.isNotBlank(cidsStr)) {
//		// sql += "and Campaigns.ID in (" + cidsStr + ") ";
//		// }
//		// sql += "ORDER BY ConfigItems.Seq ASC, ConfigItems.CampaignID ASC limit " + start + "," + each;
//		// System.out.println(sql);
//	}
//
//}
