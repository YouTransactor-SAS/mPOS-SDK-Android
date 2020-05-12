# YouTransactor mPOS SDK - Android
###### Release 3.0.0.0

![Cptr_PlatformAPI](https://user-images.githubusercontent.com/59020462/71244593-2b897180-2313-11ea-95af-8a2fcce628eb.jpeg)

This repository provides a step by step documentation for YouTransactor's native Android SDK, that enables you to integrate our proprietary card terminal(s) to accept credit and debit card payments (incl. VISA, MasterCard, American Express and more). The relation between the mobile device and the card terminal is a Master-Slave relation, so the mobile device drives the card terminal by calling diffrent available RPC. The main function of the SDK is to send RPC commands to the card terminal in order to drive it. The SDK provides also a payment, update and log APIs. 

The SDK contains several modules: Connexion, RPC, MDM, Payment, Log.
* The connexion module provide an interface 'IconnexionManager' so you can use your implementation and also it provide a Bluetooth implementaions (classical bluetooth ans BLE).
* The RPC module use the IconnexionManager implementation to send/receive, RPC command/response from card terminal. It provide an implementation of all RPC Commands you will see next how to use that in your application.
* The MDM module is an implementation of all YouTransaction's TMS services. The TMS server is mainly used to manage the version of firmware and ICC / NFC configurations of card terminal. So the SDK allows you to transparently update of the card terminal using our TMS. This module is useless if you decide to use another TMS not the YouTransactor one.
* The payment module implements the transaction processing for contact and contactless. For every payment, a UCubePaymentRequest instance should be provided as input to configure the current payment and durring the transaction a callback is returned for every step. At the end of transaction a PaymentContext instance is returned which contains all necessary data to save the transaction. An example of Payment call is provided next.
* The SDK provide an ILogger interface and a default implementation to manage logs. Your application has the choice between using the default implementation which print the logs in a file that can be sent to our TMS server or you can use you own implemantation of ILogger. 

All this functions are resumed in one Class which is UCubeAPI. This class provides public static methods that your application can use to setup ConnexionManager, setup Logger, do a payment, do an update using Our TMS...

The SDK do not perciste any connexion or transaction or update data. 

For more information about YouTransactor developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).

## I. General overview 

### 1. Introduction

YouTransactor mPOS card terminals are : 
* uCube ( with differents models )
* uCube Touch

The uCube Touch is a new version of the uCube. There are some hardware differences, like: 
* The uCube use the classical Bluetooth and the uCube Touch use the BLE 
* The uCube provide a magstripe reader but not the uCube Touch
* ...

For the SDK, there is no difference betwen all YouTransactor's card terminals. For example, if you integrate the uCube Touch, at the beginning you should use UCubeAPI to setup a BLE Connexion, and if you intergrate the uCube, you should setup a Bt classic connexion manager. So the RPC module will use the connexion manager instance that you choose to send/receive data from terminal. 

### 2. uCube

The uCube is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a Bluetooth connection to enable acceptance of magstripe, contactless and smart payment cards (depending on the model).

<p align="center">
  <img width="200" height="250" src="https://user-images.githubusercontent.com/59020462/76528252-cd32e180-6470-11ea-9182-742faca82167.png">
</p>

### 3. uCube Touch

The uCube Touch is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a BLE connection to enable acceptance of contactless and smart payment cards.

<p align="center">
  <img width="250" height="250" src="https://user-images.githubusercontent.com/59020462/77367842-437df080-6d5b-11ea-8e3a-423c3bc6b96b.png">
</p>

### 4. Mobile Device

The mobile device can be either Android or iOS and typically hosts applications related to payment. It links the uCube / uCube Touch to the rest of the system.

The mobile device application consists of 2 modules:
* Business module
	* Application that meets the business needs of the end customer. This is for example a cashier    	    application in the case of a restaurant, or a control application in the case of transports.
* Payment Module
	* Drives the transaction
	* Responsible for device software/configurations updates

The business module on the mobile device is developed by the integrator. It uses the user interfaces of the mobile device to fulfill the business needs of the customer.

The Payment module integrates our SDK, which is delivered as a library, and compiled with the payment module to generate the payment application.

The purpose of this document is to describe the services provided by the SDK to the payment module.

### 5. The Management System

The management system can be administered by YouTransactor and offers the following services:
* Management of the uCube fleet
* Deployment of software updates
* Deployment of payment parameters
* Other services

The MDM module of SDK implements all our management system services and the UCubeAPI provides API to call this implementation. Examples are provided next in this documentation.

### 6. Terminal management

#### 6.1 Initial configuration  

To be functional, in the scope of PCI PTS requirement, and SRED key shall be loaded securely in the device. This key is loaded locally by YouTransactor tools. The initial SALT is injected in the same way.

#### 6.2 Switching On/Off

The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the payment module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The uCube Touch can be lights up exactly like the uCube, but also by using ` connect`  method of the connexion manager. When connection established, the SDK checks the terminal's state, if it 's power off, it turns it ON. 

#### 6.3 Update

During the life of the terminal, the firmware could be updated (to get bug fix, evolution..), the contact and contactless configuration also could be updated. The Terminal's documentation describe how those updates can be done and which RPC to use to do that.
If you will use our TMS, this can be done transparentlly by calling first the ` mdmCheckUpdate`  API to get the TMS configuration and compare it with current versions, then the ` mdmUpdate`  to do the update.

#### 6.4 System logs

The SDK print logs in logcat at runtime. The log module use a default ILoggger implementation that print these logs in a file which can be sent afterwards to a remote server. Our TMS provides a WS to receive a zip of log files.
So you can setup the log module to use the default implementation or your own implementation. 

## II. Technical Overview

### 1. General Architecture

This diagrams describes the general YouTransactor MPOS Android SDK architecture. Only the uCubeAPI methods and the RPC commands are public and you can call them. 

![sdk_architecture](https://user-images.githubusercontent.com/59020462/81673044-5489da80-944b-11ea-95a1-ffff128a43e9.png)

### 2. Transaction Flow : Contact

![Cptr_Transaction](https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg)

### 3. Transaction Flow : Contactless

![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)

### 4. Prerequisites

To embed the package that you need in your application, you have to be sure of certain things in your settings.
1. Received YouTransactor card terminal : uCube or uCubeTouch
2. The `minSDKVersion` must be at 21 or later to works properly.
3. The `targetSDKversion` 28 or later (as a consequence of the migration to AndroidX).
4. The `Android plugin for Gradle` must be at 3.3.0 or later.
For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependency

The SDK is in the format “.aar” library. You have to copy paste it in your app/libs package. So if you want to access to it you will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/libApp.aar')

### 6. UCubeAPI

The APIs provided by UCubeAPI are:

```java

	setConnexionManager(@NonNull IConnexionManager connexionManager)
	setupLogger(@NonNull Context context, @Nullable ILogger logger)
	pay(@NonNull Activity activity, @NonNull UCubePaymentRequest uCubePaymentRequest, @NonNull UCubeLibPaymentServiceListener listener)
	close()
	
	/* YouTransactor TMS APIs*/
	mdmSetup(@NonNull Context context)
	mdmRegister(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUnregister(@NonNull Context context)
	isMdmManagerReady()
	mdmCheckUpdate(@NonNull Activity activity, boolean forceUpdate, boolean checkOnlyFirmwareVersion, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUpdate(@NonNull Activity activity, final @NonNull List<BinaryUpdate> updateList, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmSendLogs(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)

```

* You can use the sample app provided in this repository as a reference

#### 6.1 Connect Terminal

#### IConnexionManager
```java
public interface IConnexionManager {

	void setDevice(UCubeDevice UCubeDevice);

	UCubeDevice getDevice();

	boolean isConnected();

	void connect(ConnectionListener connectionListener);

	void disconnect(DisconnectListener disconnectListener);

	void send(byte[] input, SendCommandListener sendCommandListener);
}
```

* First you should set the connexion manager to the SDK using `setConnexionManager` API. 

```java
	IConnexionManager connexionManager;
	...
	
	switch (ytProduct) {
            case uCube:
                connexionManager = new BtClassicConnexionManager();
                break;

            case uCubeTouch:
                connexionManager = new BleConnectionManager();
                break;
        }
        ((BtConnectionManager) connexionManager).init(this);
	
        UCubeAPI.setConnexionManager(connexionManager);
	...
```
`BtClassicConnexionManager` and `BleConnectionManager` extend a `BtConnexionManager` which implements IConnexionManager.

* Second you should enable Bluetooth and request `ACCESS_COARSE_LOCATION`permission if you integrate uCube Touch and you will do a BLE scan. 

* Third you should select the device that you want to communicate with.
** In the case of uCube, the `BtClassicConnexionManager` provides a `public List<UCubeDevice> getPairedUCubes()` method which returns the list of paired uCube devices.
** In the case of uCube Touch, the `BleConnectionManager` provides a `public void scan(Activity activity, ScanListener scanListener)` & `public void stopScan()` methods which allow you to start and stop LE scan.
In the SampleApp an example of device selection using these methods is provided.

#### 6.2 Setup Logger

The ILogger interface : 

```java
	public interface ILogger {

	    void d(String tag, String message);

	    void e(String tag, String message, Exception e);
	}
```
To setup the log module you should put this instructions below in you App.java or MainActivity, 

```java
 	// if you want to use the default Logger
        UCubeAPI.setupLogger(this.getApplicationContext(), null);
	
	// if you want to use you Logger impl
        UCubeAPI.setupLogger(this.getApplicationContext(), new MyLogger());
```
#### 6.3 Payment

One device selected and Logger initialised, you can start using the YouTransactor SDK to accept card payments.
Durring the payment process the payment state machine will be interrupted to execute some tasks defined by you, as decribed in the Transaction Flow contact and contactless.

#### IApplicationSelectionTask
```java
public class EMVApplicationSelectionTask implements IApplicationSelectionTask {

	private List<EMVApplicationDescriptor> applicationList;
	private List<EMVApplicationDescriptor> candidateList;
	private PaymentContext context;

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
		candidateList = new ArrayList<>();

		// Todo do AID selection

		monitor.handleEvent(TaskEvent.SUCCESS); // should call this to return to the payment state machine
	}

}
```

#### IRiskManagementTask
 ```java
 public class RiskManagementTask implements IRiskManagementTask {
	private PaymentContext paymentContext;
	private byte[] tvr;

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
}
```
#### IAuthorizationTask
```java
public class AuthorizationTask implements IAuthorizationTask {
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
}
```

#### Transaction types
```java
	PURCHASE
	WITHDRAWAL
	REFUND
	PURCHASE_CASHBACK
	MANUAL_CASH
	INQUIRY
```
#### UCubePaymentRequest
```java
  List<CardReaderType> readerList = new ArrayList<>();
        readerList.add(CardReaderType.ICC);
        readerList.add(CardReaderType.NFC);

  UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
	.setAmount(15.0)
	.setCurrency(UCubePaymentRequest.CURRENCY_EUR) // Indicates the currency code of the transaction according to ISO 4217
	.setTransactionType(trxType)
	.setTransactionDate(new Date())
	.setCardWaitTimeout(timeout)
	.setDisplayResult(true) // at the end of transaction is the SDK display the payment result on uCube or just return the result
	.setReaderList(readerList) // the list of reader interfaces to activate when start the payment
	.setForceOnlinePin(true) // Applicable for NFC and MSR
	.setForceAuthorisation(true) 
	.setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)
	.setRequestedSecuredTagList(Constants.TAG_TRACK2_EQU_DATA)
	.setRequestedPlainTagList(Constants.TAG_TVR)
	.setApplicationSelectionTask(new ApplicationSelectionTask()) // if not set the SDK use the EMV default selection
	.setAuthorizationTask(new AuthorizationTask(this)) //Mandatory
	.setRiskManagementTask(new RiskManagementTask(this)) // Mandatory
	.setSystemFailureInfo(true) // get the transaction level 1 Logs
	.setSystemFailureInfo2(true) // get the transaction level 2 Logs
	.setPreferredLanguageList(Collections.singletonList("en")) // each language represented by 2 alphabetical characters according to ISO 639
	.build();
```

#### pay
```java
  UCubeAPI.pay(this, paymentRequest, new UCubeLibPaymentServiceListener() {
			@Override
			public void onProgress(PaymentState state, PaymentContext context) {}

			  @Override
			public void onFinish(boolean status, PaymentContext context) {}
		}
);

```

#### PaymentState 
```java
	/* COMMON STATES*/
	CANCEL_ALL
	GET_INFO
	WAIT_CARD //Contact only state
	ENTER_SECURE_SESSION
	KSN_AVAILABLE

	/* SMC STATES*/
	SMC_BUILD_CANDIDATE_LIST
	SMC_SELECT_APPLICATION
	SMC_USER_SELECT_APPLICATION
	SMC_INIT_TRANSACTION
	SMC_RISK_MANAGEMENT
	SMC_PROCESS_TRANSACTION
	SMC_FINALIZE_TRANSACTION
	SMC_REMOVE_CARD

	/* MSR STATES*/
	MSR_GET_SECURED_TAGS
	MSR_GET_PLAIN_TAGS
	MSR_ONLINE_PIN

	/* NFC STATES*/
	START_NFC_TRANSACTION
	NFC_GET_SECURED_TAGS
	NFC_GET_PLAIN_TAGS
	COMPLETE_NFC_TRANSACTION

	/* COMMON STATES*/
	AUTHORIZATION
	EXIT_SECURE_SESSION
	DISPLAY_RESULT
	GET_L1_LOG
	GET_L2_LOG
```
#### PaymentContext
```java
	PaymentStatus paymentStatus; // END status

	EMVApplicationDescriptor selectedApplication;
	double amount = -1;
	Currency currency;
	TransactionType transactionType;
	int applicationVersion;
	List<String> preferredLanguageList;
	byte[] uCubeInfos;
	byte[] sredKsn;
	byte[] pinKsn;
	byte activatedReader;
	boolean forceOnlinePIN;
	boolean forceAuthorization;
	byte onlinePinBlockFormat = Constants.PIN_BLOCK_ISO9564_FORMAT_0;
	int[] requestedPlainTagList;
	int[] requestedSecuredTagList;
	int[] requestedAuthorizationTagList;
	byte[] securedTagBlock;
	byte[] onlinePinBlock;
	Map<Integer, byte[]> plainTagTLV;
	byte[] authorizationResponse;
	byte[] tvr = new byte[] {0, 0, 0, 0, 0};
	Date transactionDate;
	byte[] NFCOutcome;
	byte[] transactionFinalisationData;
	byte[] transactionInitData;
	byte[] transactionProcessData;
	boolean displayResult;
	boolean getSystemFailureInfoL1, getSystemFailureInfoL2;
	byte[] systemFailureInfo; //svpp logs level 1
	byte[] systemFailureInfo2; // svpp logs level 2
```

##### PaymentStatus
```java
	NFC_MPOS_ERROR
	CARD_WAIT_FAILED
	CANCELLED
	CHIP_REQUIRED
	UNSUPPORTED_CARD
	TRY_OTHER_INTERFACE
	REFUSED_CARD
	ERROR
	APPROVED
	DECLINED
```

#### 6.4 MDM 
//Todo


### 7. RPC Commands

Once the connexionManager set and the device selected. You can call any RPC commands implemented in the SDK. This is the list of RPC Commands class: 

/* System & Drivers */
GetInfosCommand.java
SetInfoFieldCommand.java
WaitCardCommand.java
WaitCardRemovalCommand.java
DisplayChoiceCommand.java
DisplayMessageCommand.java
PowerOffCommand.java
CancelCommand.java

/* System kernel */
EnterSecureSessionCommand.java
ExitSecureSessionCommand.java
InstallForLoadCommand.java
InstallForLoadKeyCommand.java
LoadCommand.java
SimplifiedOnlinePINCommand.java

/* Payment kernel */
BankParametersDownloads.java
GetEMVParametersCommand.java
BuildCandidateListCommand.java
StartNFCTransactionCommand.java
CompleteNFCTransactionCommand.java
GetPlainTagCommand.java
GetSecuredTagCommand.java
InitTransactionCommand.java
TransactionFinalizationCommand.java
TransactionProcessCommand.java

All this commands are described in the terminal documentation.

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

![Cptr_logoYT](https://user-images.githubusercontent.com/59020462/71242500-663cdb00-230e-11ea-9a07-3ee5240c6a68.jpeg)
