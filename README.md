# JPS SDK - Android

###### Release 3.5.01

<p>
  <img src="https://user-images.githubusercontent.com/59020462/86530448-09bf9880-beb9-11ea-98f2-5ccc64ed6d6e.png">
</p>

This repository provides a step by step documentation for JPS's native Android SDK, that enables you to integrate our proprietary card terminal(s) to accept credit and debit card payments (incl. VISA, MasterCard, American Express and more). The relation between the mobile device and the card terminal is a Master-Slave relation, so the mobile device drives the card terminal by calling different available commands. The SDK is designed to provides different level of abstraction. The lowest level is "sendData" API and the highest levels are: payment, key injection, firmware & configurations update and set localization. Moreover, the SDK integrates a logger module that print and save the SDK logs.

The SDK contains several modules: Connection, RPC, MDM, Payment, RKI, Log.
* The connection module provides an interface 'IconnectionManager' so you can use your implementation and also it provides a Bluetooth implementaions (classic Bluetooth and BLE).
* The RPC module use the IconnectionManager implementation to send/receive, RPC command/response from card terminal. It provides an implementation of all RPC Commands you will see next how to use that in your application.
* The MDM module is an implementation of all JPS's TMS services. The TMS server is mainly used to manage the version of firmware and ICC / NFC configurations of card terminal. So the SDK allows you to transparently update of the card terminal using our TMS. This module is useless if you decide to use another TMS not the JPS one.
* The payment module implements the transaction processing for contact and contactless. For every payment, a UCubePaymentRequest instance should be provided as input to configure the current payment and durring the transaction a callback is returned for every step. At the end of transaction a PaymentContext instance is returned which contains all necessary data to save the transaction. An example of Payment call is provided next.
* The RKI module implements the remote key injection process. JPS provides the RKI backend, the SDK API and the terminal RPC to remotely inject the DUKPUT keys.
* The SDK provide an ILogger interface and a default implementation to manage logs. Your application has the choice between using the default implementation which print the logs in a file that can be sent to our TMS server or you can use your own implementation of ILogger.

All this functions are resumed in one Class which is UCubeAPI. This class provides public static methods that your application can use for instance to setup ConnectionManager, setup Logger, do a payment, do an update using Our TMS...etc

The SDK dont save any connection, transaction, keys or update data.

For more information about JPS products, please refer to our website. Visite [youtransactor.com!](https://www.youtransactor.com)

## I. General overview

### 1. Introduction

mPOS card terminals are :
* uCube ( with differents versions )
* YT-Touch

The YT-Touch is a new version of the uCube. There are some hardware differences, like:
* The uCube use the Classic Bluetooth and the YT-Touch use the Bluetooth Low Energy (BLE)
* The uCube provide a magstripe reader but not the YT-Touch
* ...

The support of these different products is totally transparent for the application. For example, if you integrate the YT-Touch, at the beginning you should use UCubeAPI to setup a BLE Connection Manager, and if you intergrate the uCube, you should setup a classic bluetooth connection manager. So the RPC module will use to send/receive data from terminal.

### 2. uCube

The uCube is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a Bluetooth connection to enable acceptance of magstripe, contactless and smart payment cards (depending on the model).

<p align="center">
  <img width="200" height="250" src="https://user-images.githubusercontent.com/59020462/76528252-cd32e180-6470-11ea-9182-742faca82167.png">
</p>

### 3. YT-Touch

The YT-Touch is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a BLE connection to enable acceptance of contactless and smart payment cards.

<p align="center">
  <img width="250" height="250" src="https://user-images.githubusercontent.com/59020462/77367842-437df080-6d5b-11ea-8e3a-423c3bc6b96b.png">
</p>

### 4. Mobile Device

The mobile device can be either Android or iOS and typically hosts applications related to payment. It links the card terminal to the rest of the system.

The mobile device application consists of 2 modules:
* Business module
	* Application that meets the business needs of the end customer. This is for example a cashier    	    application in the case of a restaurant, or a control application in the case of transports.
* Payment Module
	* Drives the transaction
	* Responsible for device software/configurations updates

The business module on the mobile device is developed by you. It uses the user interfaces of the mobile device to fulfill the business needs of the customer.

The Payment module integrates our SDK, which is delivered as a library, and compiled with the payment module to generate the payment application.

### 5. The Management System

The management system can be administered by JPS and offers the following services:
* Remote fleet management
* Remote software update
* Remote Bank parameters update
* Users management
* ...etc

The MDM module of SDK implements all our management system services and the UCubeAPI provides methods to invoke these services. Examples are provided next in this documentation.

### 6. Terminal management

#### 6.1 Initial configuration

To be functional, in the scope of PCI PTS requirement, SRED & Pin keys shall be loaded securely in the device. During the personalisation process JPS tools inject the certification chain. After that SRED & Pin keys can be loaded locally Or remotely using JPS Tools.

#### 6.2 Switching On/Off

The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the payment module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The YT-Touch can be light up exactly like the uCube, but also by using ` connect`  method of the connection manager. The BLE connection will automatically power on the device if it is powered off.

#### 6.3 Update

During the life cycle of the terminal, the firmware can be updated (e.g To get bug fix, evolutions..etc), the contact and contactless configuration also can be updated. The Terminal's documentation describes how these updates can be done and which RPC to use to do that.

If you will use our MDM, this can be done transparently by calling first the `mdmCheckUpdate`  method to get the TMS configuration and compare it with current versions, then the `mdmUpdate`  to download & install the new binaries.

#### 6.4 System logs

The SDK prints logs in logcat at runtime. The log module use a default ILogger implementation that prints these logs in a file which can be sent afterwards to a remote server. Our MDM exposes a web service 'sendLogs' in order to receive a zip of log files.
So you can setup the log module to use the default implementation or you create your own implementation and use it.

## II. Technical Overview

### 1. General Architecture

The diagram below describes the SDK's architecture. The Application has access to the Payment, MDM, RKI and connection modules using the uCubeAPI interface. The RPC module is public so the application has a direct access to it.

![sdk_archi](https://user-images.githubusercontent.com/59020462/182624011-97dc3bba-961b-499d-a57e-defda5184762.png)

### 2. Transaction Flow : Contact

![Cptr_Transaction](https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg)

### 3. Transaction Flow : Contactless

![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)

### 4. Prerequisites

To embed the SDK, you have to be sure of certain things in your settings.
1. The `minSDKVersion` must be at 23 or later to works properly.
2. The `targetSDKversion` 33 or later (as a consequence of the migration to AndroidX).
3. The `Android plugin for Gradle` must be at 7.4.2 or later.
   For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependencies

The SDK is in the format “.aar” library. You have to copy-paste it in your app/libs package. Then, you need to get into your app-level Build.Gradle to add this dependency:

```groovy
		implementation files('libs/libApp.aar')
```
Sample_app, from version 3.5.00 and later, need  to include SecureServiceAPI.aar to connect to secure_service.
So You have to copy-paste it in your app/libs package. Then, you need to get into your app-level Build.Gradle to add this dependency:
```groovy
		implementation files('libs/SecureServiceAPI.aar')
```
And also these dependencies :

```groovy
		implementation 'org.apache.commons:commons-lang3:3.11'
		implementation 'org.apache.commons:commons-compress:1.20'
		implementation 'com.google.code.gson:gson:2.8.6'
		implementation 'org.apache.commons:commons-io:1.3.2'
		implementation 'commons-codec:commons-codec:1.15'
		implementation 'androidx.annotation:annotation:1.2.0'
		implementation 'com.google.guava:guava:30.0-jre'
		implementation 'org.slf4j:slf4j-api:1.7.30'
		implementation 'com.github.tony19:logback-android:1.3.0-2'
```

### 6. UCubeAPI

The UCubeAPI methods are listed below:

```java
        ######################################## Initialisation APIs ######################################################
	/*
	* set the Android application context
	* the SDK will save this context in a static attribute and use it if need
	* @param context : the Android application context
	* */
	init(@NonNull Context context)
	
	/*
	* get the Application context passed before to the SDK
	* @return Context : the application context, null if the init() method was not called yet
	* */
	getContext()
	
	/*
	* stop and clean all communication with the terminal
	* */
	close()
	
	######################################## Logger APIs ######################################################
	/*
	* set the ILogger implementation that specify how the logger module should works
	* this is the ILogger interface definition
	* public interface ILogger {
	*   void d(String tag, String message);
	*   void e(String tag, String message, Exception e);
	* }
	*
	* The SDK has a default implementation which will be used if the passed parameter is null
	* The default logger print logs on logcat and save them into a log file
	* there are two kinds of logs : debug and error
	* the maximum log files that will be created in the internal storage is 5
	* The SDK creates a zip file of all created log files, which will be ready to extract
	* @param logger implementation of ILogger interface
	* */
	setupLogger(@Nullable ILogger logger)
    
	/*
	* enable or disable the SDK logs
	* depends on enable value, the SDK will call or not the d() and e()
	* functions of ILogger object
	* @param enable if true SDK print logs, otherwise SDK stop printing logs
	* by default logs are enabled
	* */
	enableLogs(boolean enable) 
  
  	/*
	* returns true if the logs are enabled otherwise returns false
	* */
  	isLogsEnabled()
	
	/*
	* set verbosity level, 
	* The levels are :
	* 	SYSTEM     = 5
	* 	CONNECTION = 4
	* 	RPC        = 3
	* 	PAYMENT    = 2
	* 	MDM        = 1
	* 	API        = 0
	* Example, if you choose CONNECTION, the SDK will print 
	* logs of CONNECTION, RPC, PAYMENT, MDM, API layers
	* @param level value
	* by default log level is API
	* */
	setLogLevel(LogManager.LogLevel level)
	
	/*
	* returns the log level set before
	* if no level was set, the returned level will be API 
	* */
	getLogLevel()
	
	######################################## Connection API ######################################################
	
	/*
	* pass the connection manager type that will define how to communicate with the device
	* the sdk will create an instance of IConnectionManager class depends on the chosen type 
	* The different ConnectionManagerType values are :
	*  enum ConnectionManagerType {
	*    BT, // uCube model
	*    BLE // YT-Touch model
        *    USB, // YT-KEY100 model
        *    SOCKET, // Simulator
        *    SOCKET_JSON, // Development environment
        *    PAYMENT_SERVICE; // Blade model
	*  }
	*
	* This is the IConnectionManager interface definition :
	* public interface IConnectionManager {
	*   void setDevice(UCubeDevice UCubeDevice);
	*   UCubeDevice getDevice();
	*   boolean isConnected();
	*   void connect(ConnectionListener connectionListener);
	*   void disconnect(DisconnectListener disconnectListener);
	*   void registerDisconnectListener(DisconnectListener disconnectListener);
	*   void send(byte[] input, SendCommandListener sendCommandListener);
	*   void close();
	* }
	* */
	setConnectionManagerType(@NonNull ConnectionService.ConnectionManagerType connectionManagerType)
	
	/*
	* get the IConnectionManager instance
	* used by the SDK to communicate with the terminal 
	* @return IConnectionManager : null if setConnectionManagerType() not be called yet
	* */
	getConnectionManager()
	
	
	/*
	* the SDK has the ability to detect the svpp restart
	* pass the listener object to the SDK, so it will be used 
	* to notify the application when this happens
	* */
	registerSVPPRestartListener(SVPPRestartListener svppRestartListener)
	
	/*
	* remove the SVPP restart listener
	* */
	unregisterSVPPRestartListener()
	
	######################################## RPC APIs ######################################################
	
	/*
	* provide the listener instance to the SDK to be notified of lost packets
	* */
	registerLostPacketListener(LostPacketListener lostPacketListener) 
	
	/*
	* remove the lostPacket listener
	* */
	unregisterLostPacketListener()
	
	/*
	* get the current sequence number value if a command with inputSecurityMode = SIGNED_CIPHERED need     
	* to be created and sent to the terminal. Only the one who has the SRED key could cipher and sign
	* the data, in the header of the command the current_sequence_number + 1 need to be add. 
	* Ref : PED Interface section 5.5.1 
	* @return int : the current sequence number value 
	* */
	getCurrentSequenceNumber()
	
	/*
	* used this api when the application need to send RPC command to the terminal
	* Note that the SDK implements an RPC module where each command is a task and can be
	* instantiated and executed. So the SDK create the data based on the variable in input,
	* the terminal state and the inputSecurityMode. Then the response is parsed base on the
	* outputSecurityMode and saved in RPCMessage object structure.
	* Ref : PED interface documentation section 6.1 describe all commands & section 3.2 describe
	* in which terminal state the command can be called
	* @param commandID : the id of command e.g. 0x5040
	* @param data : the payload of command
	* @param inputSecurityMode : the security mode of the RPCCommand data
	* @param outputSecurityMode : the securityMode of the RPCCommand response 
	* 
	* This is the different values of the SecurityMode
	* public enum SecurityMode {
	*   NONE,
	*   SIGNED_NOT_CHECKED,
	*   SIGNED,
	*   SIGNED_CIPHERED,
	* } 
	* @param uCubeLibRpcSendListener : listener to implement to get callback with the send 
	* progress and finish 
	*  */
	sendData(short commandId,
				@NonNull byte[] data,
				SecurityMode inputSecurityMode,
				SecurityMode outputSecurityMode,
				@NonNull UCubeLibRpcSendListener uCubeLibRpcSendListener)
				
	######################################## Payment API ######################################################
				
	/*
	* use this api when you need to start a transaction
	* the SDK implements the contact and the contactless transaction flow
	* @param uCubePaymentRequest : object with all needed data in input of the transaction
	* @param listener : the UCubeLibPaymentServiceListener that implement the callbacks of
	* onProgress and onFinish. The onFinish() callback has a PaymentContext object as parameters.
	* The paymentContext object is created at the begin of transaction, his input variable are set 
	* from the begin of the transaction, they can be updated during that and all output variable 
	* are set during and at the end of the transaction. 
	* */			
	EMVPaymentStateMachine pay(@NonNull UCubePaymentRequest uCubePaymentRequest, 
						@NonNull UCubeLibPaymentServiceListener listener)
						
						
	######################################## RKI API ######################################################
	
	/*
	* this api is used to inject the DUKPT keys; SRED & PIN
	* It can be used only in the Preso & Ready states of the terminal
	* */
	injectKeys(byte[] sredMode, byte[] pinMode,
                                                UCubeLibRKIServiceListener uCubeLibRKIServiceListener)
	

	######################################## MDM APIs ######################################################
	
	mdmSetup(@NonNull Context context)
	
	mdmRegister(@Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	
	mdmUnregister(@NonNull Context context)
	
	isMdmManagerReady()
	
	mdmCheckUpdate(boolean forceUpdate, 
				boolean checkOnlyFirmwareVersion, 
				@Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener
	)
	
	mdmUpdate(final @NonNull List<BinaryUpdate> updateList, 
			@Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener
	)
	
	mdmSendLogs(@Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	
	mdmGetConfig(@Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	
	
	######################################## Localisation APIs ######################################################
	
        setLocale(String locale, UCubeLibTaskListener uCubeLibTaskListener)
    
        getLocale(UCubeLibTaskListener uCubeLibTaskListener)
    
        getSupportedLocaleList(UCubeLibTaskListener uCubeLibTaskListener)
	
	#######################################################################################################
	
	
```

* You can use the sample app provided in this repository as a reference

#### 6.1 Connect Terminal

To be able to connect the terminal you need to follow these steps bellow :

* First in App class you should init the `uCubeAPI`
```java
	public class App extends Application {

	    @Override
	    public void onCreate() {
		super.onCreate();

		UCubeAPI.init(getApplicationContext());

		//Setup logger : if null lib will use it own logger
		UCubeAPI.setupLogger(null);
		
		// enable or disable SDK logs
	        UCubeAPI.enableLogs(true);
		
		// define the log level you want to activate
		UCubeAPI.setLogLevel(LogManager.LogLevel.RPC)
		...
	    }
		
		...
	}
```

* Second you should set the connection manager Type to the SDK using `setConnectionManagerType` API.

```java
	IConnectionManager connectionManager;
	...
	
	switch (ytProduct) {
            case uCube:
	        UCubeAPI.setConnexionManagerType(BT);
                break;

            case YTTouch:
                UCubeAPI.setConnexionManagerType(BLE);
                break;

            case AndroidPOS:
                UCubeAPI.setConnexionManagerType(PAYMENT_SERVICE);
                break;
        }
	...
```
You can use `UCubeAPI.getConnectionManager` API to get the IConnexionManager and call different public APIs :

```java
public interface IConnexionManager {

	List<UCubeDevice> getPairedUCubes(@Nullable String nameFilter);

	List<UCubeDevice> getPairedUCubes(@Nullable Pattern namePattern);

	void startScan(@Nullable String nameFilter, ScanListener scanListener);

	void startScan(@Nullable Pattern pattern, ScanListener scanListener);

	void stopScan();

	void setDevice(UCubeDevice UCubeDevice);

	UCubeDevice getDevice();

	boolean isConnected();

	void connect(ConnectionListener connectionListener);

	void connect(int connectionTimeoutInMills, int connectionTryCount, ConnectionListener connectionListener);

	boolean cancelConnection();

	void disconnect(DisconnectListener disconnectListener);

	void registerDisconnectListener(DisconnectListener disconnectListener);

	void registerConnectionStateListener(ConnectionStateChangeListener connectionStateChangeListener);

	void registerBatteryLevelChangeListener(BatteryLevelListener batteryLevelListener);

	void send(byte[] input, SendCommandListener sendCommandListener);

	void registerResponseListener(ResponseListener responseListener);

	Integer getBatteryLevel();

	void close();
}
```

* Third you should enable Bluetooth and request `ACCESS_COARSE_LOCATION`permission if `Build.VERSION.SDK_INT >= Build.VERSION_CODES.M` and `BLUETOOTH_SCAN` & `BLUETOOTH_CONNECT` if `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.

In the SampleApp examples of device scan, selection and connection using IConnexionManager methods are provided.

#### 6.2 Setup Logger

To setup the log module you should put this instructions below in the onCreate() function of your App class or MainActivity class.

```java
 	// if you want to use the default Logger
        UCubeAPI.setupLogger(null);
	
	// if you want to use your Logger impl
        UCubeAPI.setupLogger(new MyLogger());
```
The SDK log can be enabled or disabled using `enableLogs()` method. And `setLogLevel()` to choose the LogLevel.

#### 6.3 Payment

The SDK implement the payment state machine, both contact and contactless. You configure you transaction using the uCubePaymentRequest object by specifing a value for each attribut, for instance, the transaction amount, currency, type, ...

#### pay API
Here is the API need to be called to start a payment :

```java
  UCubeAPI.pay(this, paymentRequest, new UCubeLibPaymentServiceListener() {
	@Override
	public void onProgress(PaymentState state, PaymentContext context) {}

	  @Override
	public void onFinish(PaymentContext context) {}
  }
);
```

#### UCubePaymentRequest

The input parameter of Pay API is the uCubePaymentRequest. This class contains all input variables of a payment. At the begin of the transaction, the SDK create a new instance of PaymentContext and save into it all the input values. Here is an example of preparing a uCubePaymentRequest object, all variables are explained in the PaymentCOntext section :

```java
  int amount = 100;
  Currency currency = UCubePaymentRequest.CURRENCY_EUR;
  TransactionType trxType = TransactionType.PURCHASE;
  List<String> preferredLanguageList = Collections.singletonList("en");
  List<CardReaderType> readerList = new ArrayList<>();
  readerList.add(CardReaderType.ICC);
  readerList.add(CardReaderType.NFC);
	
  int timeout = 30;
  boolean forceOnlinePin = false; 
  boolean forceAuthorisation = false; 
  boolean byPassAuthorisation = false; 
  boolean forceDebug = false;
  boolean skipCardRemoval = true;
  boolean retrieveF5Tag = false; 
  boolean skipStartingSteps = true; 
  
  UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(
 						amount, 
						currency, 
						trxType,
						readerList, 
						new AuthorizationTask(this), 
						preferredLanguageList);
   //Add optional variables
   uCubePaymentRequest
	.setForceOnlinePin(forceOnlinePin)
	.setTransactionDate(new Date())
	.setForceAuthorisation(forceAuthorisation)
	.setBypassAuthorisation(byPassAuthorisation)
	.setRiskManagementTask(new RiskManagementTask(this))
	.setCardWaitTimeout(timeout)
	.setForceDebug(true)
        .setSkipCardRemoval(false)
	.setSkipStartingSteps(skipStartingSteps)
        .setRetrieveF5Tag(retrieveF5Tag)
	.setAuthorizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A)
	.setAuthorizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
	0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
	.setFinalizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
	0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
	.setFinalizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A);
```

#### PaymentContext
The PaymentContext is the object that evoluate for each step of the payment and it is returned at the end of the transaction using the callback onFinish().

```java
	/***************************************** input *******************************************/
	
	/* 
	* Amount of the transaction, as defined in [EMV-2]. 
	* If the Currency exponent is set to 2, and Amount is 100, 
	* the transaction price will be 1,00
	* the amount can have maximun 12 digit
	* default value is -1 means no amount will be passed byt the application
	* and the terminal will request the amount at the begin of the transaction 
	* */
    	public int amount = -1; // 
	
	/*
	* Indicates the currency of the transaction 
	* The Currency class has 3 attributes : 
	* 	String label;
	*	int code; // according to ISO 4217
	*	int exponent; 
	* */
    	public Currency currency;
	
	/*
	* Indicates the type of financial transaction, represented by the first two digits of the 
	* ISO 8583:1987 Processing Code. The actual values to be used for the Transaction Type data 
	* element are defined by the payment system. The supported Transaction type are the following one:
	*	* PURCHASE
	*	* WITHDRAWAL
	*	* REFUND
	*	* PURCHASE_CASHBACK
	*	* MANUAL_CASH
	*	* INQUIRY
	* NOTE: the support of these Transaction Type depends on the configuration. 
	* */
    	public TransactionType transactionType;
	
	/*
	* 1 to 6 languages stored in order of preference, each represented by 2 alphabetical
	* characters according to ISO 639
	* */
	public List<String> preferredLanguageList;
	
	/*
	* Local date & time that the transaction was authorised
	* */
    	public Date transactionDate;
	
	/*
	* Timeout for "waiting for any interfaces" 
	* NOTE: The timeout is limited to 0xFF (255 seconds)
	* */
    	public int cardWaitTimeout = 30; 

	/*
	* Requested PIN block format:
	*	0x00 – ISO 9564 format 0
	*	0x01 – ISO 9564 format 1
	*	0x03 – ISO 9564 format 3
	*	0x04 – ISO 9564 format 4
	*	Default is 0
	* */
    	public byte onlinePinBlockFormat = Constants.PIN_BLOCK_ISO9564_FORMAT_0; 
	
	/*
	* The different interfaces to be activated 
	* */
	public List<CardReaderType> readerList; 
	
	/*
	* only mandatory for Carte Bancaire 'CB' scheme
	* */
    	public int applicationVersion;
	
	/*
	* the list of tags need to be retrieved before calling the authorisationTask
	* */
	public int[] authorizationPlainTags, authorizationSecuredTags;
	
	/*
	* the list of tags need to be retrieved before ending the transaction
	* */
	public int[] finalizationPlainTags, finalizationSecuredTags;
    
	/*
	* skip START_CANCEL_ALL, START_EXIT_SECURE_SESSION & GET_INFO steps
	* These steps are optional to juste make sure that the device is in READY mode and not bloqued
	* */
	public boolean skipStartingSteps = false;
	
	/*
	* only for the contactless transaction, if true, force the execution of this step 
	* NFC_SIMPLIFIED_ONLINE_PIN to get the online pin block
	* */
	public boolean forceOnlinePIN = false;
	
	/*
	* For contactless, it enable the force online at the start nfc transaction 
	* for the contact, it enable the Merchant force online : byte 4 bit 4 of the TVR
	* */
    	private boolean forceAuthorization = false;
	
	/*
	* if true, the SDK will retrieve the 0xF4 and 0xCC tags at the end of the transaction 
	* the 0xF4 and 0xCC tags contain part of SVPP Level 2 logs
	* */
	public boolean forceDebug = false;
	
	/*
	* if true, the SDK will retrieve the 0xF4 and 0xCC tags at the end of the transaction
	* this flag can be enabled by the application during one of the tasks for instance the 
	* authorisationTask if the backend decide to decline the transaction.
	* */
	public boolean getSystemFailureInfoL2 = false;
	
	/*
	* if true, the SDK will retrieve the 0xF5 after a STRT_NFC_TRANSACTION command fails
	* the 0xF5 tag contains part of the SVPP Level 2 logs
	* */
	public boolean retrieveF5Tag = false;

	/*
	* if true, the SDK will skip the SMC_REMOVE_CARD and does not wait for card to be removed 
	* and go through the transaction result
	* */
    	public boolean skipCardRemoval = false;
    
	/*
	* This variable need to be set after calling the authorisation backend 
	* It contains the host response
	* */
        public byte[] authorizationResponse;
	
	
        /***************************************** output *******************************************/
	
    	/* output common */
    	public PaymentStatus paymentStatus; // The payment status see below possible values 
    	public byte[] uCubeInfos; // If the GET_INFO was called, it will contains the terminal firmware version
    	public byte[] sredKsn; // Key serial number (SMID) includes current transaction key counter for the data encryption
    	public byte[] pinKsn; // MANDATORY if an Online PIN has been entered.Null if not.
    	public byte[] onlinePinBlock; // The returned pin block formatted with the onlinePinBlockFormat specified in the input 
    	public byte activatedReader; // The activated interface 
    	public Map<Integer, byte[]> finalizationPlainTagsValues; // A map of key value that contains all requested finalization plain tags
    	public Map<Integer, byte[]> authorizationPlainTagsValues; // A map of key value that contains all requested authorisation plain tags
    	public byte[] finalizationGetPlainTagsResponse; // the whole terminal's response of the getPlainTags command to be checked by the backend
    	public byte [] finalizationSecuredTagsValues; // the whole terminal's response of the getSecuredTags command to be checked & parsed by the backend
    	public byte[] authorizationGetPlainTagsResponse; //the whole terminal's response of the getPlainTags command to be checked by the backend
    	public byte [] authorizationSecuredTagsValues; // the whole terminal's response of the getSecuredTags command to be checked & parsed by the backend
    	
    	/* output icc */
	/*
	* The object that describe the selected application
	* the EMVApplicationDescriptor has these attributes : 
	*	private byte[] aid;
	*	private String label;
	*	private int priority;
	*	private int issuerCodeIndex;
	*	private byte selectionOptions;
	*	private boolean blocked;
	*	private byte[] languagePreference;
	*	private byte[] fci;
	*	private byte[] productId;
	* */
    	public EMVApplicationDescriptor selectedApplication;
	
	/*
	* The terminal verification result
	* */
    	public byte[] tvr = new byte[] {0, 0, 0, 0, 0};
	
	/*
	* The whole response of the TransactionFinalisation command with header and footer 
	* */
    	public byte[] transactionFinalisationData;
	
	/*
	* The whole response of the TransactionInitialization command with header and footer 
	* */
    	public byte[] transactionInitData;
	
	/*
	* The whole response of the TransactionProcess command with header and footer 
	* */
    	public byte[] transactionProcessData;
	
	/*
	* The OUTCOME is composed of 2 bytes:
	*	Byte 0:
	*	- 0x30: Receipt
	*	- 0x31: Receipt, Signature required
	*	- 0x32: No Receipt
	*	- 0x34: Online PIN Request
	*	Byte 1:
	*	- 0x36: APPROVED 
	*	- 0x3E: ONLINE_REQUEST 
	*	- 0x31: TRY_ANOTHER_INTERFACE
	*	- 0x3A: TRANSACTION_CANCELLED 
	*	- 0x3F: END_APPLICATION 
	*	- 0x37: DECLINED 
	*	- 0x38: FAILED 
	* Example: "0x3036" 
	*	
	* */
    	public byte[] nfcOutcome;
	
	/*
	* When the byte 0 of the outcome equals to 0x31, 
	* this flag will be enabled and the application nned to 
	* request the cardholder signature
	* */
    	public boolean signatureRequired;
	
    	/* output for debug */
    	public byte[] tagCC; // svpp logs level 2 Tag CC
    	public byte[] tagF4; // svpp logs level 2 Tag F4
    	public byte[] tagF5; // svpp logs level 2 Tag F5
```

#### PaymentState
You will receive the onProgress() callback for each new state. This is the whole list of payment states :

```java
	/* COMMON STATES*/
	//Start
	START_CANCEL_ALL,
	START_EXIT_SECURE_SESSION,
	GET_INFO,
	ENTER_SECURE_SESSION,
	KSN_AVAILABLE,
	START_TRANSACTION,
	WAITING_CARD,
	CARD_READ_END,

	//Authorization
	AUTHORIZATION,
	
	//PIN
	ONLINE_PIN,
	OFFLINE_PIN,

	//END
	GET_FINALIZATION_SECURED_TAGS,
	GET_FINALIZATION_PLAIN_TAGS,
	GET_CC_L2_LOG,
	GET_F4_L2_LOG,
	GET_F5_L2_LOG,
	END_EXIT_SECURE_SESSION,

	/* SMC STATES*/
	START_ICC,
	SMC_BUILD_CANDIDATE_LIST,
	SMC_SELECT_APPLICATION,
	SMC_USER_SELECT_APPLICATION,
	SMC_INIT_TRANSACTION,
	SMC_RISK_MANAGEMENT,
	SMC_PROCESS_TRANSACTION,
	SMC_GET_AUTHORIZATION_SECURED_TAGS,
	SMC_FINALIZE_TRANSACTION,
	SMC_REMOVE_CARD,

	/* NFC STATES*/
	START_NFC,
	NFC_GET_AUTHORIZATION_SECURED_TAGS,
	NFC_SIMPLIFIED_ONLINE_PIN,
	NFC_COMPLETE_TRANSACTION,
```

#### EMV Payment state machine

![Document sans titre](https://user-images.githubusercontent.com/59020462/110345361-c5c7f900-802e-11eb-9748-94ddd0645aab.png)

The EMV payment state machine is sequence of executing commands and tasks. Bellow you will see the different tasks used at transaction

#### Tasks
Durring the payment process the payment state machine will be interrupted to execute some tasks that you implement.

##### IApplicationSelectionTask

```java
public class EMVApplicationSelectionTask implements IApplicationSelectionTask {

	private List<EMVApplicationDescriptor> applicationList;
	private List<EMVApplicationDescriptor> candidateList;
	private PaymentContext context;
	protected ITaskMonitor monitor;

	@Override
	public void setAvailableApplication(List<EMVApplicationDescriptor> applicationList) {
		this.applicationList = applicationList;
	}

	@Override
	public List<EMVApplicationDescriptor> getSelection() {
		return candidateList;
	}

	@Override
	public PaymentContext getContext() {
		return context;
	}

	@Override
	public void setContext(PaymentContext paymentContext) {
		this.context = paymentContext;
	}

	@Override
	public void execute(ITaskMonitor monitor) {
		this.monitor = monitor;
		
		candidateList = new ArrayList<>();

		// Todo do AID selection

		monitor.handleEvent(TaskEvent.SUCCESS); // should call this to return to the payment state machine
	}
	
	@Override
	public void cancel(ITaskCancelListener taskCancelListener) {
		monitor.handleEvent(TaskEvent.CANCELLED);
		taskCancelListener.onCancelFinish(true);
	}
}
```
##### IRiskManagementTask
 ```java
 public class RiskManagementTask implements IRiskManagementTask {
	private PaymentContext paymentContext;
	private byte[] tvr;
	private ITaskMonitor monitor;

	@Override
	public byte[] getTVR() {
		return tvr;
	}

	@Override
	public PaymentContext getContext() {
		return paymentContext;
	}

	@Override
	public void setContext(PaymentContext context) {
		this.paymentContext = context;
	}

	@Override
	public void execute(ITaskMonitor monitor) {
		this.monitor = monitor;

		//TODO perform risk management 
		
		monitor.handleEvent(TaskEvent.SUCCESS); // should call this to return to the payment state machine
	}
	
	@Override
	public void cancel(ITaskCancelListener taskCancelListener) {
		monitor.handleEvent(TaskEvent.CANCELLED);
		taskCancelListener.onCancelFinish(true);
	}
}
```
##### IAuthorizationTask
```java
public class AuthorizationTask implements IAuthorizationTask {
    	private ITaskMonitor monitor;
	
	@Override
	public byte[] getAuthorizationResponse() {
		return authResponse;
	}

	@Override
	public PaymentContext getContext() {
		return paymentContext;
	}

	@Override
	public void setContext(PaymentContext context) {
		this.paymentContext = context;
	}

	@Override
	public void execute(ITaskMonitor monitor) {
		this.monitor = monitor;

		// TODO perform the authorisation

		monitor.handleEvent(TaskEvent.SUCCESS); // should call this to return to the payment state machine
	}
	
	@Override
	public void cancel(ITaskCancelListener taskCancelListener) {
		monitor.handleEvent(TaskEvent.CANCELLED);
		taskCancelListener.onCancelFinish(true);
	}
}
```

##### PaymentStatus
```java
    APPROVED, // Transaction has been approved by terminal
    DECLINED, // Transaction has been declined by terminal
    /* Cancelled Status cases:
        1/ GPO not read yet and application calls payment.cancel()
        2/ one of commands returns -32 or -28 status
        3/ NFC_Outcome[1] = 0x3A Transaction_cancelled
    */
    CANCELLED,

    CARD_WAIT_FAILED,//Transaction has been failed because customer does not present a card and startNFCTransaction fail
    UNSUPPORTED_CARD, ///Transaction has been failed: Error returned by terminal, at contact transaction, when no application match between card and terminal's configuration

    NFC_OUTCOME_TRY_OTHER_INTERFACE, // Transaction has been failed: Error returned by terminal, at contactless transaction
    NFC_OUTCOME_END_APPLICATION,// Transaction has been failed: Error returned by terminal, at contactless transaction
    NFC_OUTCOME_FAILED,// Transaction has been failed: Error returned by terminal, at contactless transaction

    ERROR, // Transaction has been failed : when one of the tasks or commands has been fail
    ERROR_DISCONNECT,//Transaction has been failed : when there is a disconnect during the transaction
    ERROR_SHUTTING_DOWN,//Transaction has been failed : when command fails with SHUTTING_DOWN error during the transaction
    ERROR_WRONG_ACTIVATED_READER, // Transaction has been failed : when terminal return wrong value in the tag DF70 at startNFCTransaction
    ERROR_MISSING_REQUIRED_CRYPTOGRAM,// Transaction has been failed :when the value of the tag 9f27 is wrong
    ERROR_WRONG_CRYPTOGRAM_VALUE, // Transaction has been failed : when in the response of the transaction process command the tag 9F27 is missing
    ERROR_WRONG_NFC_OUTCOME, // Transaction has been failed : when terminal returns wrong values in the nfc outcome byte array
}
```
##### Localization
```java
public class Localization {
	private static Map<Integer, String> localizationMap;

	public static void Localization_init(Context ctx) {
		localizationMap = new HashMap<>();
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_AMOUNT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AMOUNT));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_AMOUNT_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AMOUNT_OK));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_APPROVED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_APPROVED));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CALL_YOUR_BANK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CALL_YOUR_BANK));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CANCEL_OR_ENTER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CANCEL_OR_ENTER));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CARD_ERROR.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CARD_ERROR));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_DECLINED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_DECLINED));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_ENTER_AMOUNT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_ENTER_AMOUNT));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_ENTER_PIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_ENTER_PIN));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_INCORRECT_PIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INCORRECT_PIN));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_NOT_ACCEPTED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NOT_ACCEPTED));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PIN_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PIN_OK));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PLEASE_WAIT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PLEASE_WAIT));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PROCESSING_ERROR.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PROCESSING_ERROR));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_REMOVE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_REMOVE_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_USE_CHIP_READER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_USE_CHIP_READER));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_USE_MAG_STRIPE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_USE_MAG_STRIPE));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRY_AGAIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRY_AGAIN));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_WELCOME.getId(), ctx.getString(R.string.SCR_CLESS_MSG_WELCOME));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PROCESSING.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PROCESSING));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CARD_READ_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CARD_READ_OK));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_OR_SWIPE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_OR_SWIPE_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_ONLY_ONE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_ONLY_ONE_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_APPROVED_SIGN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_APPROVED_SIGN));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_AUTHORIZING_WAIT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AUTHORIZING_WAIT));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_SWIPE_ANOTHER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_SWIPE_ANOTHER));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CL_INSERT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CL_INSERT_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_CLEAR_DISPLAY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CLEAR_DISPLAY));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_SEE_PHONE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_SEE_PHONE));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_CARD_AGAIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_CARD_AGAIN));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_LAST_PIN_TRY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_LAST_PIN_TRY));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_NO_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NO_CARD));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_TOO_MANY_TRIES.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TOO_MANY_TRIES));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_PAYMENT_NOT_ACCEPTED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PAYMENT_NOT_ACCEPTED));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_LIMIT_REACHED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_LIMIT_REACHED));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CT_Y.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CT_Y));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_Y.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_Y));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_N.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_N));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_NONE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NONE));
		addLocalizedMsg(MessageId.SCR_CLESS_MSG_GC_PLACE_CARD_ON_TRAY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_GC_PLACE_CARD_ON_TRAY));
		addLocalizedMsg(MessageId.SCR_CT_FIRST_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_FIRST_PIN_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_WRONG_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_PIN_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_LAST_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_LAST_PIN_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_PIN_BLOCKED_TEXT.getId(), ctx.getString(R.string.SCR_CT_PIN_BLOCKED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_PIN_OK_TEXT.getId(), ctx.getString(R.string.SCR_CT_PIN_OK_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_INTERMEDIATE_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_INTERMEDIATE_PIN_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_AUTHORIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_AUTHORIZATION_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_SMC_INITIALIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_INITIALIZATION_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_SMC_RISK_MANAGEMENT_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_RISK_MANAGEMENT_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_SMC_FINALIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_FINALIZATION_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_SMC_REMOVE_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_REMOVE_CARD_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_NFC_COMPLETE_TEXT.getId(), ctx.getString(R.string.SCR_CT_NFC_COMPLETE_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_WAIT_ONLINE_PIN_PROCESS_TEXT.getId(), ctx.getString(R.string.SCR_CT_WAIT_ONLINE_PIN_PROCESS_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_APPROVED_TEXT.getId(), ctx.getString(R.string.SCR_CT_APPROVED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_DECLINED_TEXT.getId(), ctx.getString(R.string.SCR_CT_DECLINED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_UNSUPPORTED_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_UNSUPPORTED_CARD_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_REFUSED_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_REFUSED_CARD_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_NO_CARD_DETECTED_TEXT.getId(), ctx.getString(R.string.SCR_CT_NO_CARD_DETECTED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_FAILED_TEXT.getId(), ctx.getString(R.string.SCR_CT_FAILED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_END_APPLICATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_END_APPLICATION_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_TRY_ANOTHER_TEXT.getId(), ctx.getString(R.string.SCR_CT_TRY_ANOTHER_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_WRONG_NFC_OUTCOME_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_NFC_OUTCOME_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_WRONG_CRYPT_VALUE_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_CRYPT_VALUE_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_MISSING_REQUIRED_CRYPTO_TEXT.getId(), ctx.getString(R.string.SCR_CT_MISSING_REQUIRED_CRYPTO_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_APPROVED_SIG_TEXT.getId(), ctx.getString(R.string.SCR_CT_APPROVED_SIG_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_CANCELED_TEXT.getId(), ctx.getString(R.string.SCR_CT_CANCELED_TEXT));
		addLocalizedMsg(MessageId.SCR_CT_REMOVE_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_REMOVE_CARD_TEXT));
	}

	public static void addLocalizedMsg(int key, String value) {
		localizationMap.put(key, value);
	}

	public static String getLocalizedMsg(int key) {
		return localizationMap.get(key);
	}

	public static boolean IsExistInLocalizationMap(int key) {
		return localizationMap.containsKey(key);
	}

	public static String getMsg(int key, String msg) {
		if(!IsExistInLocalizationMap(key)){
			return msg;
		}
		else{
			return getLocalizedMsg(key);
		}
	}
}
```
#### Cancel Payment
During the transaction, Customer may need to cancel payment. This is only possible before terminal reads card with success, in other words the GPO of card was successfully read. The cancel method returns a callback with status of cancellation. Here is a figure that resume the two kind of states, blue ones the cancellation is possoble the red ones the cancellation not possible. Note that at the end of startTransaction state, if the reader interface was NFC, so the card  was successfully read. The startTransaction step do the wait card and the read card for contactless and only the wait card for contact.

![payment states](https://user-images.githubusercontent.com/59020462/110348022-7e8f3780-8031-11eb-96a3-35c67997a7e2.png)

```java
            EMVPaymentStateMachine emvPaymentStateMachine = UCubeAPI.pay(...);
	    
	   ....
	   emvPaymentStateMachine.cancel(ITaskCancelListener taskCancelListener);
```   

#### 6.4 RKI

The SDK encapsulate the different communication with the terminal and the backend in order to facilitate your integration. You only have to use the injectkeys API like the following example :

```java
	UCubeAPI.injectKeys(TDES, TDES, new UCubeLibRKIServiceListener() {
            @Override
            public void onProgress(RKIServiceState state) {
	        // update the UI here
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                // Check status and update UI
            }
        });
```

#### 6.5 MDM

#### Setup

The main function of MDM module is the update of firmware and configurations of terminal to do that you have to setup this module :

* First The MDM module need to be setup by you application context using this UCUbeAPI method :

```java
	UCubeAPI.mdmSetup(this);
```
* Second the terminal have to be registred on the TMS server using this code below :
```java
 UCubeAPI.mdmRegister(this, new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
               // Todo UI
            }

            @Override
            public void onFinish(boolean status, Object... params) {
               // Todo UI
	       // params is empty for this API
            }
        });
```
At the register process the SDK send the public certificate of terminal to the TMS, so the server can verifie the JPS signature and then generate and return an SSL certificate unique by terminal. This SSL certificate is used to call the rest of web services.
Note that the register should be done only once, at the selection of terminal. the SDK save the SSL certificate and to be removed you have to call this method below.

```java
	boolean res = UCubeAPI.mdmUnregister(this);
	if(!res) {
		Log.e(TAG, "FATAL Error! error to unregister current device");
	}
```
To check if the SSL certificate exit, use this method :

```java 
	UCubeAPI.isMdmManagerReady() 
```
#### Update

The update is done in two steps, check the TMS configuration and compare it with current versions this is performed by the `mdmCheckUpdate` method and then download the binary(ies) from TMS server and install them and this can be done by the `mdmUpdate` method.

The mdmCheckUpdate's onFinish() callback returns two list :
- params[0] : List<Config> : the server's configuration
- params[1] : List<BinaryUpdate> : After comparing the terminal's current configuration to the the server's configuration this is the result. It will be the input of the mdmUpdate method.

```java 
boolean checkOnlyFirmwareVersion = false;
boolean forceUpdate = false;

UCubeAPI.mdmCheckUpdate(activity, forceUpdate, checkOnlyFirmwareVersion,
	new UCubeLibMDMServiceListener() {
		@Override
		public void onProgress(ServiceState state) {
		///TODO UI
		}

		@Override
		public void onFinish(boolean status, Object... params) {
		if (status) {
		    List<Config> cfgList = (List<Config>) params[0];
		  
		    List<BinaryUpdate> updateList = (List<BinaryUpdate>) params[1];
		    if (updateList.size() == 0) {
			Toast.makeText(this, "Terminal up to date" , Toast.LENGTH_SHORT).show();
		     } else {
			// Todo call mdmUpdate with in input a List<BinaryUpdate>
		     }
		}
	});
});
```

```java 
 UCubeAPI.mdmUpdate(this, selectedUpdateList, new UCubeLibMDMServiceListener() {
	@Override
	public void onProgress(ServiceState state) {
	    //TODO UI
	}

	@Override
	public void onFinish(boolean status, Object... params) {
	    //TODO UI
	}
});
```

#### Send Logs

Sending Logs to the server is useful in case of debug. the TMS server provides a web service to receive these log files and the SDK implement the call of this ws.

```java 
UCubeAPI.mdmSendLogs(this, new UCubeLibMDMServiceListener() {
    @Override
    public void onProgress(ServiceState state) {
	//TODO UI
    }

    @Override
    public void onFinish(boolean status, Object... params) {
    	//TODO UI
    }
});
```

### 7. RPC Commands

Once the connexionManager set and the device selected. You can call any RPC commands implemented in the SDK. Al commands are described in details in the PED Interfaces document section 6.

This is the list of RPC Commands class:

```java
/************************************ System & Drivers ************************************/

/*
 * The command gets a set of device informations. 
 * */
GetInfosCommand.java

/*
 * The command set device information. Note, this command can be called once the device is in the field. 
 * */
SetInfoFieldCommand.java

/*
 * This function waits for card insertion on a set of slots, in parallel. 
 * In case a smart card is inserted, the card is powered on automatically (cold reset).
 * Note: This command can be cancelled by the cardholder (Cancel key pressed by the cardholder)
 * */
WaitCardCommand.java


/*
 * This command sends a power off to a smart card previously inserted. 
 * */
WaitCardRemovalCommand.java

/*
 * This command is used to display a list box. The keys ABORT and OK are evaluated during this command. 
 * The list is build up in the order the text strings are given. 
 * A clear screen is performed before and after each command execution in order to erase remaining previous text
 * */
DisplayChoiceCommand.java

/*
 * This command is used to display a message on the screen without user Key Input. 
 * Only the OK, CANCEL and ABORT keys are monitored, and returned back. 
 * The text is given in command parameters. The command will answer only when:
    • The timeout is reached or
    • One of the configured “abortkey” is pressed.
 * The command defines a full display. A clear screen is performed before each command execution 
 * in order to erase remaining previous text.
 * */
DisplayMessageCommand.java

/*
 * This command is used to power off the device. Additionally, to this command there is an automatic
 * power off after a defined timeout. The timeout can be set with the Set Device Info command. 
 * */
PowerOffCommand.java

/*
 * This command is used to cancel all asynchronous process.
 * */
CancelCommand.java

/************************************ System kernel ************************************/

/*
 * If this command is called during the:
 *   • READY state: the product switches from READY state to SECURED state. 
 *   This command will internally increment the DUKPT key counter and generates session keys for a new transaction.
 *   All internal data or states which may be left from a previous transaction are cleared during the call.
 *   • PERSO state: the product switches from PERSO to READY. 
 *   It can no more switching back to PERSO state any more after this action.
 *   • PRE PERSO state: the product switches from PRE PERSO to PERSO. 
 *   It can no more switching back to PRE PERSO state any more after this action.
 * */
EnterSecureSessionCommand.java

/*
 * This command is used to switch from SECURED state to READY state. 
 * All internal data or states which may be left from the previous transaction are cleared during the call.
 * */
ExitSecureSessionCommand.java

/*
 * This command initializes a download sequence. 
 * */
InstallForLoadCommand.java

/*
 * This command is used to export all the necessary information to the RKI server 
 * in order to perform the further DUKPT initial key injection. 
 * */
InstallForLoadKeyCommand.java

/*
 * This command sends the Data File to load. This command is sent in several Blocks. 
 * Once the last block is received by the SVPP, the code installed once the signature has been verified.
 * */
LoadCommand.java

/*
 * This command processes an online PIN entry and return back the encrypted PIN block using the DUKPT PIN session key.
 * */
SimplifiedOnlinePINCommand.java

/************************************ Payment kernel ************************************/

/*
 * Before executing any transaction, a set of banking parameters must be initialized. 
 * These banking parameters provided to this function are only the one handled by EMVL2 
 * (AID list for application selection public keys to perform cards offline authentication, 
 * and Certificate Revocation List: CRL). 
 * Note that the parameters provided by the bank, but corresponding to the local payment scheme 
 * should be handled by the calling application.
 * */
BankParametersDownloads.java

/*
 * This command retrieve previously set bank parameters. 
 * */
GetEMVParametersCommand.java

/*
 * This function starts a NFC Transaction.
 * */
StartNFCTransactionCommand.java

/*
 * This function completes a NFC Transaction (if necessary)
 * */
CompleteNFCTransactionCommand.java

/*
 * This function returns the value of one given tag. 
 * The tag can be an EMV tag or a proprietary tag. 
 * */
GetPlainTagCommand.java

/*
 * This function returns the value of one given tag. 
 * The tag can be an EMV tag or a proprietary tag. 
 * NOTE: If only one TAG is provided in input, the output will ONLY contain the value (and not the format TLV)
 * SRED tags: 56 – 57 - 5A – 5F20 – 5F24 – 5F30 – 9F0B – 9F6B
 * */
GetSecuredTagCommand.java

/*
 * The execution of this command presumes that a card is already inserted in the device, and powered on.
 * This command process the matching between the EMV applications supported by the terminal, and the one 
 * supported by the card (with card supporting PSE or list of AID) with the Build candidate process list 
 * defined in [EMV-1], section 12.3. It builds the candidate list and returns the mutually supported applications
 * list with, for each application, the AID, the preferred name ( if the issuer table index is supported, 
 * see [ ISO/IEC 8859] ) or the label, the application priority indicator and the issuer table index. 
 * This command provides back all the necessary information to enable the calling application to choose the
 * appropriated AID according to its local scheme specificities. If more than one application can be chosen, 
 * the calling application will ask for cardholder choice through the DisplayListBox function. 
 * Once the AID selected, the PAY.TransactionInitialization command is called with this AID in input to continue
 * the transaction sequence.
 * */
BuildCandidateListCommand.java

/*
 * The execution of this command presumes that a card is already inserted in the device, and powered on, and the
 * BuildCandidateList has previously been called.
 * This command:
 *   • Initializes the transaction, with an amount, currency, etc.
 *   • Performs the FINAL SELECTION, with the Final selection according to [EMV-1], section 12.4
 *   • Performs the GET PROCESSING OPTIONS followed by the subsequent READ RECORDS, according to [EMV-3], 
 *     section 10.1 & 10.2.
 *   • Select the cardholder language. In case a card language matches with a language provided by 
 *   the calling application, it is automatically selected. In case of multiple choices, 
 *   a DisplayListBox appears on the screen to ask the user to select his preferred language.
 * This command provides back all the necessary information to enable the calling application to 
 * perform an acquire risk management (black list, b check, etc...). Once the result of the 
 * risk management is known, the calling application sends the TransactionProcess to continue 
 * the transaction sequence.
 * */
InitTransactionCommand.java

/*
 * The execution of this command presumes that a card is already inserted in the device,
 * and powered on, and the PAY.TransactionInitialization has previously been called.
 * This command:
 *   • Performs the PROCESSING RESTRICTIONS according to [EMV-3], section 10.4
 *   • Performs the PIN ENTRY according to [PCI PTS] security requirements, according to the cardholder 
 *     selected language during PAY.TransactionInitialization, and according to [EMV-3], section 10.5
 *   • Performs the OFFLINE DATA AUTHENTICATION (SDA, DDA, SDA) according to [EMV-3], section 10.3
 *   • Performs the TERMINAL RISK MANAGEMENT according to [EMV-3], section 10.6 (Floor Limits, 
 *      Random Transaction Selection, Velocity Checking)
 *   • Performs the TERMINAL ACTION ANALYSIS (first generate AC) according to [EMV-3], section 10.7
 * This command provides back all the necessary information to enable the calling application to perform 
 * an authorisation to the acquire
 * */
TransactionProcessCommand.java

/*
 * The execution of this command presumes that a card is already inserted in the device, and powered on,
 * and the TransactionProcess has previously been called.
 * This command:
 *   • Performs the ISSUER DATA AUTHENTICATION according to [EMV-3], section 10.9 (Online processing / external
 *   authenticate)
 *   • Performs the ISSUER SCRIPT PRCESSING according to [EMV-3], section 10.10. It
 *       ◦ Applies SCRIPT 71
 *       ◦ Performs the SECOND GENERATE AC
 *       ◦ Applies SCRIPT 72
 * This command provides back all the necessary information to finalise a transaction. 
 * Note, at the end of this command, even if the transaction is accepted, the calling application
 * can later refuse it (for instance if the cardholder receipt printing failed).
 * */
TransactionFinalizationCommand.java
```

* This is an example of command call:

```java
	DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand(msg);

	displayMessageCommand.setCentered(centred);
	displayMessageCommand.setYPosition(yPosition);
	displayMessageCommand.setFont(font);

	displayMessageCommand.execute(new ITaskMonitor() {

		@Override
		public void handleEvent(TaskEvent event, Object... params) {
		switch (event) {
			case FAILED:
			//TODO
			break;

		case SUCCESS:
			//TODO
			break;
		}
		}
	});
```

In which state of the terminal command can be called ? This is described in the PED Interfaces document section 6.2.
* If the device is in secured state, the input / output data may be protected by a specific security level. The terminal documentation describe how input data and output data are protected for every command in each different security state. There are four different protection level :
	* None
	* Signed but the uCube don't check the signature // Only for input
	* Signed
	* Signed and ciphered

* In the case of Input, for the two fist levels, you can use the RPC commands classes. SDK will manage the creation of the payload you have juste to set different values in different attribut of class. But, if the level is signed or signed and ciphered the whole of the command data should be created by the HSM server. Then your application should call UCubeAPI.sendData().

This is an example :

```java 
     UCubeAPI.sendData(
                this,
                Constants.INSTALL_FOR_LOAD_COMMAND,
                payload,
                SecurityMode.SIGNED_CIPHERED,
                SecurityMode.SIGNED,
                new UCubeLibRpcSendListener() {
                    @Override
                    public void onProgress(RPCCommandStatus rpcCommandStatus) {
                        Log.d(TAG, "On progress : " + rpcCommandStatus);
                    }

                    @Override
                    public void onFinish(boolean status, byte[] response) {
		        //response contains the whole of the ucube response ( no parsing is done ) 
                        Log.d(TAG, "On finish : " + status + " Reponse : " + Tools.bytesToHex(response));
                    }
                });
		
```
Note : In the secure session there is  a sequence number managed by the SDK and incremented at every RPC call, If you need to know what is the current sequence number you cann get it using `getCurrentSequenceNumber` API.

* In the case of output, the SDK create e RPCMessage to store response.

```java
public class RPCMessage {

	private short commandId;
	private short status;
	private byte[] data;
	private byte[] data_mac; /* The MAC when secured */
	private byte[] data_ciphered; /* The Ciphered data with the crypto header when secured ( but without the MAC ) */
	private byte[] buffer; /* contains the whole response of ucube without parsing */

}	
```
* Switch case of protection level, the parse of response will be different :
	* In the case of none, it will be the same parse as Ready state, only `commandId, status & data` contain values.
	* In the case of signed, `commandId, status, data & data_mac` contain values.
	* In the case of signed and ciphered, `commandId, status, data, data_mac & data_ciphered` contain values.

Note that no MAC if the data is null.


### 8. Display events

For Android product, the secure Firmware will send display events to notify Android application about transaction view updates.

You must register to the DisplayEventListener to receive display events.

```java
    /*
     * pass the listener object to the SDK to be notified if display events occurs
     * */
    public static void setDisplayEventListener(EventListener listener)
```

You have to pass an EventListener defined as follow:

```java
	public interface EventListener {
	    void onEvent(EventCommand event);
	}
```

You will receive some EventCommand objects:

```java
	public abstract class EventCommand extends RPCCommand {
	    protected Event event;
	
	    public Event getEvent() {
	        return this.event;
	    }
	
	    public EventCommand(short commandId, SecurityMode inputSecurityMode, SecurityMode outputSecurityMode) {
	        super(commandId, inputSecurityMode, outputSecurityMode);
	    }
	}
```

The events are defined by the Event enum type:
```java
	public enum Event {
	    dsp_wait_card,
	    dsp_payment_result,
	    dsp_update_cless_LED,
	    dsp_read_card,
	    dsp_authorisation,
	    dsp_update_KDB_change,
	    dsp_listbox,
	    dsp_idle;
	}
```

Each event has an appropriate class. You can get event properties from it.

#### 8.1 WaitCard event

```java
	public class EventDspWaitCard extends EventCommand {
	    private int[] interfaces;
	    private String amount;
	    private String message;
	
	    public int[] getInterfaces() {
	        return this.interfaces;
	    }
	    public String getAmount() {
	        return this.amount;
	    }
	    public String getMessage() {
	        return this.message;
	    }
	}
```

#### 8.2 PaymentResult event

```java
	public class EventDspPaymentRslt extends EventCommand {
	    private String message;
	
	    private int result;
	
	    public String getMessage() {
	        return this.message;
	    }
	
	    public int getResult() {
	        return this.result;
	    }
	}
```

#### 8.3 UpdateClessLed event

```java
	public class EventDspUpdateClessLed extends EventCommand {
	    private int statusLed1 = 0;
	    private int statusLed2 = 0;
	    private int statusLed3 = 0;
	    private int statusLed4 = 0;
	
	    public int getStatusLed1() {
	        return this.statusLed1;
	    }
	
	    public int getStatusLed2() {
	        return this.statusLed2;
	    }
	
	    public int getStatusLed3() {
	        return this.statusLed3;
	    }
	
	    public int getStatusLed4() {
	        return this.statusLed4;
	    }
```

#### 8.4 ReadCard event

```java
	public class EventDspReadCard extends EventCommand {
	    private String message;
	
	    public String getMessage() {
	        return this.message;
	    }
	}
```

#### 8.5 Authorisation event

```java
	public class EventDspAuthorisation extends EventCommand {
	    private String message;
	
	    public String getMessage() {
	        return this.message;
	    }
	}
```

#### 8.6 Listbox event

Not yet implemented.

#### 8.7 Idle event

No parameters.

### 9. Speed Mode

Starting with Firmware version 6.0.0.54, a new tag was added `Constants.TAG_FC_SPEED_MODE` to get and set the BLE speed mode.

The `GetInfoCommand` is used to retrieve the current speed mode. Example :

```java
    GetInfosCommand command = new GetInfosCommand(Constants.TAG_FC_SPEED_MODE);
		command.execute((event, params) -> {
		switch (event1) {
		case PROGRESS:
		break;

		case FAILED:
		break;

		case CANCELLED:
		break;

		case SUCCESS:
		break;
		}
		});
```

The `SetInfoFieldCommand` is used to define the speed mode. Example :

```java
    SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
		setInfoFieldCommand.setMode(SLOW_MODE); // or QUICK_MODE
		setInfoFieldCommand.execute((event1, params1) -> {
		switch (event1) {
		case PROGRESS:
		break;

		case FAILED:
		break;

		case CANCELLED:
		break;

		case SUCCESS:
		break;
		}
		});
```
### 10. Firmware update

```java
	UpdateItem item = new UpdateItem()
	                    .label(“firmware ”) /* free label */
	                    .data(<InputStream>) /* binary data */
	                    .signature(<InputStream>); /* binary data signature */
```


```java
	final List<UpdateItem> updateList = new ArrayList<>();
	
	/* add all needed UpdateItem to the list */
	updateList.add(item)
	
	final ITaskMonitor monitor = (event, params) -> {
	            if ( event != TaskEvent.PROGRESS) {
	                updateInProgress = false;
			/* update ended either success or failed depending on the TaskEvent */
	            }
	
			/* param contains the associated UpdateItem */
	            String msg = params.length > 0 && params[0] instanceof ServiceState
	                    ? ((ServiceState) params[0]).name()
	                    : event.name();
	
			/* do whatever needed with event param */
	            displayState(msg, event == TaskEvent.PROGRESS);
	        };
	
	        Executors.newSingleThreadExecutor().execute(() -> {
	            LocalUpdateService lus = new LocalUpdateService();
	            lus.execute(updateList, monitor);
	        });
	
	LocalUpdateService lus = new LocalUpdateService();
	lus.execute(updateList, monitor);
```

![Cptr_logoYT](https://user-images.githubusercontent.com/59020462/71242500-663cdb00-230e-11ea-9a07-3ee5240c6a68.jpeg)
