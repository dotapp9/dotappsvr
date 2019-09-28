package org.tharak.app;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tharak.dot.util.DataSourceMgr;
import org.tharak.dot.util.IndexWrapper;
import org.tharak.dot.util.WholeWordIndexFinder;


import lombok.AllArgsConstructor;
@RequestMapping("/api")
@RestController
@AllArgsConstructor
public class DotEndpoint {
	private DataSourceMgr mgr;
	@RequestMapping(value = Constants.SERVICE_PATH, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> doDelegate(Principal principal, @PathVariable String version, @PathVariable String serviceId, @RequestHeader Map<String, String> rqHeaders, @RequestBody Map<String, String> requestBody, HttpSession httpSession)throws IOException {
		return doBodyDelegate(principal, serviceId, rqHeaders, requestBody, httpSession);
	}
	@GetMapping(path = Constants.HEALTH_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> doHealth(Principal principal, @PathVariable String version, HttpServletRequest req)throws IOException {
		return new ResponseEntity<String>("Ready", HttpStatus.OK);
	}
	private List<Map<String, String>> doBodyDelegate(Principal principal, String serviceId, Map<String, String> rqHeaders, Map<String, String> requestBody, HttpSession httpSession) {
		String query = requestBody.get("query");
		TreeMap<Integer, String> attrMap = new TreeMap<>();
		WholeWordIndexFinder queryFinder = new WholeWordIndexFinder(query);
		WholeWordIndexFinder finder = new WholeWordIndexFinder(query);
		Iterator<String> itr = requestBody.keySet().iterator();
		while(itr.hasNext()) {
			String searchParam = itr.next();
			if("query".equals(searchParam))
				continue;
			queryFinder.findIndexesForKeyword("$"+searchParam, true);
			List<IndexWrapper> indexes = finder.findIndexesForKeyword("$"+searchParam);
			if(!indexes.isEmpty()) {
				IndexWrapper wrapper = indexes.get(0);
				int idx = wrapper.getStart();
				String value = requestBody.get(searchParam);
				attrMap.put(idx, value);
			}
		}
		return executeStmt(queryFinder.getSearchString(), attrMap);
	}
	private List<Map<String, String>> executeStmt(String query, Map<Integer, String> attrMap) {
		Connection dbcon = null;
		PreparedStatement pstmt = null;
		ResultSet rsTables = null;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		try {
			dbcon = mgr.getConnection();
			pstmt = dbcon.prepareStatement(query);
			Iterator<Integer> itr = attrMap.keySet().iterator();
			int idx=1;
			while(itr.hasNext()) {
				Integer key = itr.next();
				pstmt.setString(idx, attrMap.get(key));
				idx++;
			}
			if(query.startsWith("select") || query.startsWith("SELECT")) {
				rsTables = pstmt.executeQuery();
				while(rsTables.next()) {
					ResultSetMetaData rsMd = rsTables.getMetaData();
					HashMap<String, String> resultMap = new HashMap<String, String>();
					for(int clx=0; clx<rsMd.getColumnCount(); clx++) {
						resultMap.put(rsMd.getColumnName(clx+1), rsTables.getString(clx+1));
					}
					resultList.add(resultMap);
				}
			}else {
				int count = pstmt.executeUpdate();
				HashMap<String, String> resultMap = new HashMap<String, String>();
				resultMap.put("record_count", String.valueOf(count));
				resultList.add(resultMap);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			try {
				if(dbcon != null)
					dbcon.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}finally {
			mgr.close(null, pstmt, dbcon);
		}
		return resultList;
	}
}
