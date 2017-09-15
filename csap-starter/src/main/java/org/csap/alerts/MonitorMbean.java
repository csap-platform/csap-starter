package org.csap.alerts;

import org.csap.integations.CsapPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@ManagedResource(objectName = MonitorMbean.PERFORMANCE_MBEAN, description = "Exports performance data to external systems")
public class MonitorMbean {

	static final Logger logger = LoggerFactory.getLogger( MonitorMbean.class );

	public final static String PERFORMANCE_MBEAN = "org.csap:application=CsapPerformance,name=PerformanceMonitor";
	
	
	
	public MonitorMbean() {
	}
	

	
	public enum Report {
		undefined( "undefined" ), pending( "pendingFirstInterval" ),
		healthy( "isHealthy" ),
		collectionCount( "collectionCount" ), limitsExceeded( "limitsExceeded" ),
		lastCollected("lastCollected");

		public String json;

		private Report( String jsonField ) {
			this.json = jsonField;
		}
	}
	



	@Autowired
	AlertProcessor alertProcessor ;

	/**
	 *
	 * HealthChecks will invoke via JMX - false will trigger alerts.
	 *
	 * @return
	 */

	@ManagedMetric(category = "PERFORMANCE ", displayName = "isHealthy", description = "Iterates over configured simons checking configured limits", metricType = MetricType.GAUGE)
	public boolean getHealthStatus () {

		return alertProcessor.getHealthReport().get( Report.healthy.json ).asBoolean() ;

	}
	
	@ManagedMetric(category = "PERFORMANCE ", displayName = "HealthReport", description = "Iterates over configured simons checking configured limits", metricType = MetricType.GAUGE)
	public String getHealthReport () {

		
		return alertProcessor.getHealthReport().toString();

	}
	

}

