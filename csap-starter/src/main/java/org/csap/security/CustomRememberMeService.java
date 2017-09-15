package org.csap.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.csap.helpers.CsapRestTemplateFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

public class CustomRememberMeService extends TokenBasedRememberMeServices {

	final static Logger logger = LoggerFactory.getLogger( CustomRememberMeService.class );
	
    public CustomRememberMeService(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }
	
	
	// Hook for sharing cookie across service instances
	public static String getSingleSignOnDomain(HttpServletRequest request) {
		return request.getServerName().substring( request.getServerName().indexOf( "." ) + 1 ) ;
	}
    
    /**
     * 
     * Overrid to use Session cookie single sign on in domain
     */
    protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(getCookieName(), cookieValue);
        // cookie.setMaxAge(maxAge);  // make it a session cookie
        // cookie.setDomain(".yourcompany.com") ;
        cookie.setDomain(getSingleSignOnDomain( request ) ) ;
        cookie.setPath("/") ;
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
    protected UserDetails processAutoLoginCookie(String[] cookieTokens,
			HttpServletRequest request, HttpServletResponse response) {
    	
    	logger.debug( "Processing SSO" );
    	Split split = SimonManager.getStopwatch("csap.security.rememberMe").start();
    	
    	UserDetails result = null;
		try {
			result = super.processAutoLoginCookie( cookieTokens, request, response );
	    	split.stop();
		} catch (Exception e) {

	    	split.stop();
			if ( e instanceof InvalidCookieException  ) {

				logger.debug( "SSO Expiration: {}",
					CsapRestTemplateFactory.getFilteredStackTrace( e, "csap" ) ); 
			} else {
				
				logger.warn( "Failed processing login. Validate csapSecurity.property settings: {}",
					CsapRestTemplateFactory.getFilteredStackTrace( e, "csap" ) ); 
			}

			throw e;
		}
    	
    	return result ;
    }
}
