package com.sgcc.cloud.user;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgcc.cloud.entity.User;
import com.sgcc.cloud.entity.WeChatSession;
import com.sgcc.cloud.user.dao.UserRepository;

@RestController
public class UserController {
	
	private static final String APPID = "wx78d88d1881aa87d5";
	private static final String SECRET = "34fd3cc0765ed7397d0677473031f2eb";

    private String code;
    
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@GetMapping("{id}")
	public User findById(@PathVariable  Long id) {
		User findOne = userRepository.findOne(id);
		return findOne;
	}
	
	@GetMapping("weapp/login")
	public WeChatSession login(HttpServletRequest request) {
//		Long id = (long) 1;
//		User findOne = userRepository.findOne(id);
		Map<String, String> map = getHeadersInfo(request);
		String code = map.get("x-wx-code");
		 //微信的接口
		 String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+APPID+
				 "&secret="+SECRET+"&js_code="+ code +"&grant_type=authorization_code";
//	    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+APPID
//	    		+"&secret="+SECRET+"&code="+ code +"&grant_type=authorization_code";
		 System.out.println(url);
		 RestTemplate restTemplate = new RestTemplate();
		 //进行网络请求,访问url接口
	     ResponseEntity<String>  responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);  
	     WeChatSession weChatSession = null;
	     //根据返回值进行后续操作 
	     if(responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK)  
	        {
	    	 	String sessionData = responseEntity.getBody();
	    	 	System.out.println(sessionData);
//	    	 	Gson gson = new Gson();
	    	 	//解析从微信服务器获得的openid和session_key;
	    	 	try {
	    	 		weChatSession = objectMapper.readValue(sessionData, WeChatSession.class);
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//	    	 	WeChatSession weChatSession = gson.fromJson(sessionData, WeChatSession.class);
	    	 	//获取用户的唯一标识
	    	 	String openid = weChatSession.getOpenid();
	    	 	//获取会话秘钥
	    	 	String session_key = weChatSession.getSession_key();
	    	 	//下面就可以写自己的业务代码了
	    	 	//最后要返回一个自定义的登录态,用来做后续数据传输的验证
	    	 	
	            String $url = "https://api.weixin.qq.com/sns/userinfo?access_token="+session_key+"&openid="+openid;//+"&lang=zh_CN";
	            ResponseEntity<String>  userInfoEntity = restTemplate.exchange($url, HttpMethod.GET, null, String.class);
	        System.out.println($url);
	            if(userInfoEntity != null && userInfoEntity.getStatusCode() == HttpStatus.OK)  
		        {
	            	String userInfo = userInfoEntity.getBody();
		    	 	System.out.println(userInfo);
		    	 	/*try {
		    	 		weChatSession = objectMapper.readValue(sessionData, WeChatSession.class);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
		        }
	        }
	     
	     return weChatSession;
	}

	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
	    Map<String, String> map = new HashMap<String, String>();
	    Enumeration headerNames = request.getHeaderNames();
	    while (headerNames.hasMoreElements()) {
	        String key = (String) headerNames.nextElement();
	        String value = request.getHeader(key);
	        map.put(key, value);
	    }
	    return map;
	  }

}
