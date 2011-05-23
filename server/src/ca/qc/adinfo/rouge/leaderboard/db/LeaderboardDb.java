/*
 * Copyright [2011] [ADInfo, Alexandre Denault]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.qc.adinfo.rouge.leaderboard.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import ca.qc.adinfo.rouge.data.RougeObject;
import ca.qc.adinfo.rouge.leaderboard.Leaderboard;
import ca.qc.adinfo.rouge.leaderboard.Score;
import ca.qc.adinfo.rouge.server.DBManager;
import ca.qc.adinfo.rouge.user.User;
import ca.qc.adinfo.rouge.variable.Variable;

public class LeaderboardDb {
	
	private static Logger log = Logger.getLogger(LeaderboardDb.class);
	
	public static boolean createLeaderboard(DBManager dbManager, String key, String name) {
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		sql = "INSERT INTO rouge_leaderboards (`key`, `name`) VALUES (?, ?);";
		
		try {
			connection = dbManager.getConnection();
			stmt = connection.prepareStatement(sql);
			
			stmt.setString(1, key);
			stmt.setString(2, name);
						
			int ret = stmt.executeUpdate();
			
			return (ret > 0);
			
		} catch (SQLException e) {
			log.error(stmt);
			log.error(e);
			return false;
			
		} finally {
		
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(connection);
		}
	}
	
	public static boolean submitScore(DBManager dbManager, String key, long userId, long score) {
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		sql = "INSERT INTO rouge_leaderboard_score (`leaderboard_key`, `user_id`, `score`) " +
				"VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE score = MAX(?, score);";
		
		try {
			connection = dbManager.getConnection();
			stmt = connection.prepareStatement(sql);
			
			stmt.setString(1, key);
			stmt.setLong(2, userId);
			stmt.setLong(3, score);
			stmt.setLong(4, score);
						
			int ret = stmt.executeUpdate();
			
			return (ret > 0);
			
		} catch (SQLException e) {
			log.error(stmt);
			log.error(e);
			return false;
			
		} finally {
		
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(connection);
		}
	}
	
	public static Leaderboard getLeaderboard(DBManager dbManager, String key) {
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT score, user_id FROM rouge_leaderboard_score " +
				"WHERE `leaderboard_key` = 'TEST_LB' ORDER BY `score` DESC LIMIT 5;";
		
		try {
			connection = dbManager.getConnection();
			stmt = connection.prepareStatement(sql);
			
			stmt.setString(1, key);
			
			Leaderboard leaderboard = new Leaderboard(key);
					
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				Score score = new Score(rs.getLong("userId"), rs.getLong("score"));
				leaderboard.addScore(score);
			}
			
			return leaderboard;
			
		} catch (SQLException e) {
			log.error(stmt);
			log.error(e);
			return null;
			
		} finally {
		
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(connection);
		}
	}
	
	public static HashMap<String, Leaderboard> getLeaderboards(DBManager dbManager) {
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		HashMap<String, Leaderboard> returnValue = new HashMap<String, Leaderboard>();
		
		String sql = "SELECT key FROM rouge_leaderboards;";
		
		try {
			connection = dbManager.getConnection();
			stmt = connection.prepareStatement(sql);
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				
				String key = rs.getString("key");
				Leaderboard leaderboard = getLeaderboard(dbManager, key);
				
				if (leaderboard == null) {
					returnValue.put(key, new Leaderboard(key));
				} else {
					returnValue.put(key, leaderboard);
				}
			}
			
			return returnValue;
			
		} catch (SQLException e) {
			log.error(stmt);
			log.error(e);
			return null;
			
		} finally {
		
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(connection);
		}
	}
	
	

}
