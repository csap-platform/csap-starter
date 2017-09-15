
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.csap.helpers.CsapShellConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.JOptCommandLinePropertySource;
import org.springframework.core.io.ClassPathResource;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.HelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Sample invokations:
 * -debug -lab http://csap-dev01.your-company.com:8080/admin -api services/byName/CsAgent -parseOutput -jpath /0/port
 * -debug -parseOutput -textResponse -lab http://csap-dev01.your-company.com:8011/CsAgent -api service/log/filter -params serviceName_port=CsAgent_8011,userid=csapeng.gen,pass=csaprest123!,fileName=consoleLogs.txt,filter=Start
 *
 * Look in Junits, and code samples in csap definition scripts
 *
 * LB seems to generate unknown host on windows -lab https://csap-secure.your-company.com/admin -api hosts
 *
 * @author pnightin
 *
 */
public class CsapShell {

	public static void main(String[] args) {

		logInit();

		LoggerFactory.getLogger( CsapShell.class ).info( "Got params: " + Arrays.asList( args ).toString() );

		StringBuffer sbuf = new StringBuffer();
		sbuf.append( "\n\t JVM Classpath is: "
				+ System.getProperty( "java.class.path" ) );
		// System.out.println( sbuf );

		// http://pholser.github.io/jopt-simple/examples.html
		OptionParser parser = new OptionParser() {
			{
				acceptsAll( Arrays.asList( "h", "?", "help" ), "show help" ).forHelp();

				HelpFormatter formatter = new BuiltinHelpFormatter( 300, 3 );
				formatHelpWith( formatter );

				accepts( "lab", "Url target of lab for command" ).withOptionalArg().ofType( String.class )
						.defaultsTo( "http://localhost:8011/CsAgent" );

				accepts( "params", "http post parameters with form name1=value1,name2=value2,..." ).withOptionalArg()
						.ofType( String.class ).defaultsTo( "none" );

				accepts( "debug", "logs will be output along with result" );
				accepts( "debugAll", "logs will be output along with result" );

				accepts( "textResponse", "Result is plain text. If not present application/json is assumed" );

				accepts( "timeOutSeconds", "time to wait for response, default is 5 seconds" ).withOptionalArg()
						.ofType( String.class ).defaultsTo( "5" );

				accepts( "file", "File to load content for validate api" ).withOptionalArg().ofType( String.class )
						.defaultsTo( "none" );

				accepts( "script", "Parse output, same as parseOutput" );

				accepts( "parseOutput", "Parse output, same as parse" );

				accepts( "jpath",
						"Use path to extract values from JSON. Refer to http://tools.ietf.org/html/draft-ietf-appsawg-json-pointer-03" )
						.withRequiredArg().ofType( String.class );

				OptionSpec<String> apiParam = accepts( "api", "api to be invoked" ).withOptionalArg()
						.ofType( String.class ).defaultsTo( "help" );
			}
		};

		OptionSet options = parser.parse( args );
		if ( options.has( "debug" ) ) {
			Configurator.setLevel( "org.csap", Level.DEBUG );
			Configurator.setLevel( CsapShell.class.getName(), Level.DEBUG );
		} else if ( options.has( "debugAll" ) ) {
			Configurator.setLevel( "", Level.DEBUG );
		}
		//
		//
		if ( options.has( "help" ) ) {
			LoggerFactory.getLogger( CsapShell.class ).info( "Found help" );
			try {
				parser.printHelpOn( System.out );
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			LoggerFactory.getLogger( CsapShell.class ).info( "Invoking CsapShellConfiguration using CommandLinePropertySource" );
			CommandLinePropertySource<OptionSet> clps = new JOptCommandLinePropertySource( options );
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.getEnvironment().getPropertySources().addFirst( clps );
			ctx.register( CsapShellConfiguration.class );
			ctx.refresh();
			CsapShellConfiguration shellContext = ctx.getBean( CsapShellConfiguration.class );
			shellContext.execute();
		}

	}

	private static void logInit() {
		URI configFile = null;
		String log4jConfigFileName = "log4jShell.yaml";
		try {
			configFile = new ClassPathResource( log4jConfigFileName ).getURI();
			//PropertyConfigurator.configure(new ClassPathResource("log4jShell.properties").getInputStream());
			Configurator.initialize( null, CsapShell.class.getClassLoader(), configFile );
			LoggerFactory.getLogger( CsapShell.class ).info( "Configured: {}", configFile );
		} catch ( Exception e ) {
			System.out.println( "Failed to locate: " + log4jConfigFileName );
			e.printStackTrace();
		}

	}

}
