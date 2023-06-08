package core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

	private AppConfig() {}

	public enum Machine {
		local		("0.0.0.0", "8443", "4723"),
		stg      	("0.0.0.0", "4444", "4723"),
		sauce		(sauceLabsUser + ":" + sauceLabsKey + "@ondemand.saucelabs.com", "80", "");

		private final String address;
		private final String webPort;
		private final String mobilePort;

		Machine(String address, String webPort, String mobilePort) {
			this.address = address;
			this.webPort = webPort;
			this.mobilePort = mobilePort;
		}

		public String getAddress() { return this.address; }
		public String getAddressWeb() { return this.address + ":" + this.webPort; }
		public String getAddressMobile() { return this.address + ":" + this.mobilePort; }
	}


	public enum testEnvironment {
		local		("0.0.0.0:8443", "/test/git-repos/testmobile/test-android-pos/build/outputs/apk/" + appFilename),
		stage		("test-stage.herokuapp.com", "s3.amazonaws.com/test-stage/static-content/apks/" + appFilename),
		prod		("www.test.com", "test.com/link/apk");

		private final String webPath;
		private final String apkPath;

		testEnvironment(String webPath, String apkPath) {
			this.webPath = webPath;
			this.apkPath = apkPath;
		}

		public String getWebPath() { return this.webPath; }
		public String getAPKPath() { return this.apkPath; }
	}


	private static final String browser 					= getProperty("browser");
	private static final String appFilename 				= getProperty("appFilename");
	private static final String deviceName 					= getProperty("deviceName");
	private static final String restaurant 					= getProperty("restaurant");
	private static final String buildJobName 				= getProperty("buildJobName");
	private static final String buildJobNumber 				= getProperty("buildJobNumber");
	private static final Machine machine				 	= Machine.valueOf(getProperty("machine"));
	private static final testEnvironment testEnv			= testEnvironment.valueOf(getProperty("testEnvironment"));
	private static final String packageName					= getProperty("packageName");


	private static final String sauceLabsUser 				= "testqa";
	private static final String sauceLabsKey				= "";
	private static final String appPackage 					= "com.test.pos";
	private static final String appActivity 				= ".RootActivity";

	private static int testNumber 							= 0;
	private static String testName 							= null;


	public static testEnvironment gettestEnv() { return testEnv; }

	public static Machine getMachine() { return machine; }

	public static String getBaseUrl() { return "https://" + testEnv.getWebPath(); }

	public static String getBrowser() { return browser; }

	public static String getDeviceName() { return deviceName; }

	public static String getAppPackage() { return appPackage; }

	public static String getAppActivity() { return appActivity; }

	public static String getRestaurant() { return restaurant; }

	public static String getSauceLabsUser() { return sauceLabsUser; }

	public static String getSauceLabsKey() { return sauceLabsKey; }

	public static String getBuildJobName() { return buildJobName; }

	public static String getBuildJobNumber() { return buildJobNumber; }

	public static String getJenkinsIp() { return "172.16.50.50"; }

	public static String getJenkinsPort() { return "8080"; }

	public static int getTestNumber() { return testNumber; }

	public static void setTestNumber(int newTestNumber) { testNumber = newTestNumber; }

	public static String getTestName() { return testName; }

	public static void setTestName(String newTestName) {
		testName = newTestName;
	}

	public static String getPackageName() { return packageName; }


	private static String getProperty(String property) {
		Properties prop = new Properties();

		try {

			InputStream input = new FileInputStream("resources/config.properties");
			prop.load(input);

		} catch (IOException ex) {
			throw new RuntimeException("Failed to read property : " + property);

		}

		return prop.getProperty(property);
	}

}