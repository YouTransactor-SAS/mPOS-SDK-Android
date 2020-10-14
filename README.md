# YouTransactor mPOS SDK - Android

###### Release 3.4.1

<p>
  <img src="https://user-images.githubusercontent.com/59020462/86530448-09bf9880-beb9-11ea-98f2-5ccc64ed6d6e.png">
</p>

This repository provides a step by step documentation for YouTransactor's native Android SDK, that enables you to integrate our proprietary card terminal(s) to accept credit and debit card payments (incl. VISA, MasterCard, American Express and more). The relation between the mobile device and the card terminal is a Master-Slave relation, so the mobile device drives the card terminal by calling diffrent available commands. The main function of the SDK is to send RPC commands to the card terminal in order to drive it. The SDK provides also a payment, update and log APIs. 

The SDK contains several modules: Connexion, RPC, MDM, Payment, Log.
* The connexion module provides an interface 'IconnexionManager' so you can use your implementation and also it provides a Bluetooth implementaions (classic Bluetooth and BLE).
* The RPC module use the IconnexionManager implementation to send/receive, RPC command/response from card terminal. It provides an implementation of all RPC Commands you will see next how to use that in your application.
* The MDM module is an implementation of all YouTransactor's TMS services. The TMS server is mainly used to manage the version of firmware and ICC / NFC configurations of card terminal. So the SDK allows you to transparently update of the card terminal using our TMS. This module is useless if you decide to use another TMS not the YouTransactor one.
* The payment module implements the transaction processing for contact and contactless. For every payment, a UCubePaymentRequest instance should be provided as input to configure the current payment and durring the transaction a callback is returned for every step. At the end of transaction a PaymentContext instance is returned which contains all necessary data to save the transaction. An example of Payment call is provided next.
* The SDK provide an ILogger interface and a default implementation to manage logs. Your application has the choice between using the default implementation which print the logs in a file that can be sent to our TMS server or you can use your own implementation of ILogger. 

All this functions are resumed in one Class which is UCubeAPI. This class provides public static methods that your application can use to setup ConnexionManager, setup Logger, do a payment, do an update using Our TMS...

The SDK do not save any connexion or transaction or update data. 

For more information about YouTransactor developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).

## I. General overview 

### 1. Introduction

YouTransactor mPOS card terminals are : 
* uCube ( with differents models )
* uCube Touch

The uCube Touch is a new version of the uCube. There are some hardware differences, like: 
* The uCube use the Classic Bluetooth and the uCube Touch use the Bluetooth Low Energy (BLE)
* The uCube provide a magstripe reader but not the uCube Touch
* ...

For the SDK, there is no difference betwen all YouTransactor's card terminals. For example, if you integrate the uCube Touch, at the beginning you should use UCubeAPI to setup a BLE Connexion Manager, and if you intergrate the uCube, you should setup a classic bluetooth connexion manager. So the RPC module will use to send/receive data from terminal. 

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

The management system can be administered by YouTransactor and offers the following services:
* Management of the uCube fleet
* Deployment of software updates
* Deployment of payment parameters
* Other services

The MDM module of SDK implements all our management system services and the UCubeAPI provides methods to call this implementation. Examples are provided next in this documentation.

### 6. Terminal management

#### 6.1 Initial configuration  

To be functional, in the scope of PCI PTS requirement, and SRED key shall be loaded securely in the device. This key is loaded locally by YouTransactor tools. The initial SALT is injected in the same way.

#### 6.2 Switching On/Off

The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the payment module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The uCube Touch can be lights up exactly like the uCube, but also by using ` connect`  method of the connexion manager. When connection established, the SDK checks the terminal's state, if it 's power off, it turns it ON. 

#### 6.3 Update

During the life of the terminal, the firmware could be updated (to get bug fix, evolutions..), the contact and contactless configuration also could be updated. The Terminal's documentation describe how these updates can be done and which RPC to use to do that.

If you will use our TMS, this can be done transparentlly by calling first the ` mdmCheckUpdate`  method to get the TMS configuration and compare it with current versions, then the ` mdmUpdate`  to download & intall the binary update.

#### 6.4 System logs

The SDK prints logs in logcat at runtime. The log module use a default ILogger implementation that prints these logs in a file which can be sent afterwards to a remote server. Our TMS provides a WS to receive a zip of log files.
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

To embed the SDK, you have to be sure of certain things in your settings.
1. The `minSDKVersion` must be at 21 or later to works properly.
2. The `targetSDKversion` 28 or later (as a consequence of the migration to AndroidX).
3. The `Android plugin for Gradle` must be at 3.3.0 or later.
For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependency

The SDK is in the format “.aar” library. You have to copy-paste it in your app/libs package. So if you want to use his public APIs you will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/libApp.aar')

### 6. UCubeAPI

The APIs provided by UCubeAPI are:

```java
	init(@NonNull Context context)
	getContext()
	close()
	setConnexionManager(@NonNull IConnexionManager connexionManager)
	setupLogger(@Nullable ILogger logger)
	enableLogs(boolean enable)
	getCurrentSequenceNumber()
	sendData(@NonNull Activity activity,
				short commandId,
				@NonNull byte[] data,
				SecurityMode inputSecurityMode,
				SecurityMode outputSecurityMode,
				@NonNull UCubeLibRpcSendListener uCubeLibRpcSendListener)
	EMVPaymentStateMachine pay(@NonNull Activity activity, @NonNull UCubePaymentRequest uCubePaymentRequest, @NonNull UCubeLibPaymentServiceListener listener)

	/* YouTransactor TMS APIs*/
	mdmSetup(@NonNull Context context)
	mdmRegister(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUnregister(@NonNull Context context)
	isMdmManagerReady()
	mdmCheckUpdate(@NonNull Activity activity, boolean forceUpdate, boolean checkOnlyFirmwareVersion, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUpdate(@NonNull Activity activity, final @NonNull List<BinaryUpdate> updateList, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmSendLogs(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmGetConfig(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)

```

* You can use the sample app provided in this repository as a reference

#### 6.1 Connect Terminal

The IConnexionManager interface : 

```java
public interface IConnexionManager {

	void setDevice(UCubeDevice UCubeDevice);

	UCubeDevice getDevice();

	boolean isConnected();

	void connect(ConnectionListener connectionListener);

	void disconnect(DisconnectListener disconnectListener);

	void send(byte[] input, SendCommandListener sendCommandListener);

	void close();
}
```
* First in App class you should init the `uCubeAPI`
```java
	public class App extends Application {

	    @Override
	    public void onCreate() {
		super.onCreate();

		UCubeAPI.init(getApplicationContext());

		//Setup logger : if null lib will use it own logger
		UCubeAPI.setupLogger(null);
		
		...
	    }
		
		...
	}

```

* Second you should set the connexion manager to the SDK using `setConnexionManager` API. 

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

* Third you should enable Bluetooth and request `ACCESS_COARSE_LOCATION`permission if you integrate uCube Touch and you want to do a BLE scan. 

* Then you should select the device that you want to communicate with.
	* In the case of uCube, the `BtClassicConnexionManager` provides a `public List<UCubeDevice> getPairedUCubes()` method which returns the list of paired uCube devices.
	* In the case of uCube Touch, the `BleConnectionManager` provides a `public void scan(Activity activity, ScanListener scanListener)` & `public void stopScan()` methods which allow you to start and stop LE scan.
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
        UCubeAPI.setupLogger(null);
	
	// if you want to use your Logger impl
        UCubeAPI.setupLogger(new MyLogger());
```
The SDK log can be enabled or disabled using `enableLogs` method. 

#### 6.3 Payment

#### Transaction types
This is the different transaction type that the solution authorise.

```java
	PURCHASE
	WITHDRAWAL
	REFUND
	PURCHASE_CASHBACK
	MANUAL_CASH
	INQUIRY
```

#### pay API
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

The input parameter of Pay API is the uCubePaymentRequest.
```java
  List<CardReaderType> readerList = new ArrayList<>();
        readerList.add(CardReaderType.ICC);
        readerList.add(CardReaderType.NFC);

  UCubePaymentRequest paymentRequest = new UCubePaymentRequest(15.0, UCubePaymentRequest.CURRENCY_EUR,
    trxType, readerList, altMsgBundle, msgBundle, 
    new AuthorizationTask(this), Collections.singletonList("en")
  
	paymentRequest
    .setForceOnlinePin(forceOnlinePin)
    .setTransactionDate(new Date())
    .setDisplayResult(displayResultOnUCube)
    .setForceAuthorisation(forceAuthorisation)
    .setRiskManagementTask(new RiskManagementTask(this))
    .setCardWaitTimeout(timeout)
    .setSystemFailureInfo(false)
    .setSystemFailureInfo2(false)
    .setAuthorizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A)
    .setAuthorizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
         0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
    .setFinalizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
         0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
    .setFinalizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A);
```

#### PaymentContext
The PaymentContext is the object that evoluate for each step of the payment and is returned at the end.

```java
	/* input */
	public int cardWaitTimeout = 30;
	public double amount = -1;
	public Currency currency;
	public TransactionType transactionType;
	public Date transactionDate;
	public int applicationVersion; // Mandatory for Carte Bancaire 'CB' scheme
	public List<String> preferredLanguageList;
	public boolean forceOnlinePIN;
	public boolean forceAuthorization;
	public byte onlinePinBlockFormat = Constants.PIN_BLOCK_ISO9564_FORMAT_0;     
	public List<CardReaderType> readerList;
	public ResourceBundle msgBundle;
	public Bundle altMsgBundle;
	public boolean displayResult = true;
	public boolean getSystemFailureInfoL1, getSystemFailureInfoL2;
	public int[] authorizationPlainTags, authorizationSecuredTags;
	public int[] finalizationPlainTags, finalizationSecuredTags;
	
	/* output common */
	public PaymentStatus paymentStatus;
	public byte[] uCubeInfos;
	public byte[] sredKsn;
	public byte[] pinKsn;
	public byte[] onlinePinBlock;
	public byte activatedReader;
	public Map<Integer, byte[]> finalizationPlainTagsValues;
	public byte [] finalizationSecuredTagsValues;
	public Map<Integer, byte[]> authorizationPlainTagsValues;
	public byte [] authorizationSecuredTagsValues;
	public byte[] authorizationResponse;
	
	/* output icc */
	public EMVApplicationDescriptor selectedApplication;
	public byte[] tvr = new byte[] {0, 0, 0, 0, 0};
	public byte[] transactionFinalisationData;
	public byte[] transactionInitData;
	public byte[] transactionProcessData;
	
	/* output nfc */
	public byte[] nfcOutcome;
	public boolean signatureRequired;
	
	/* output for debug */
	public byte[] systemFailureInfo; //svpp logs level 1
	public byte[] systemFailureInfo2; // svpp logs level 2
```

#### PaymentState 
You will receive the onProgress() callback for each new state. This is the whole liste of payement states : 

```java
	/* COMMON STATES*/
	//start
	START_EXIT_SECURE_SESSION,
	DISPLAY_WAIT_PREPARE_TRANSACTION,
	GET_INFO,
	ENTER_SECURE_SESSION,
	KSN_AVAILABLE,
	START_TRANSACTION,
	
	//authorization
	AUTHORIZATION,
	
	//end
	END_EXIT_SECURE_SESSION,
	DISPLAY_RESULT,
	GET_L1_LOG,
	GET_L2_LOG,

	/* SMC STATES*/
	SMC_DISPLAY_WAIT_INIT_TRANSACTION,
	SMC_BUILD_CANDIDATE_LIST,
	SMC_SELECT_APPLICATION,
	SMC_USER_SELECT_APPLICATION,
	SMC_INIT_TRANSACTION,
	SMC_DISPLAY_WAIT_RISK_MANAGEMENT_PROCESSING,
	SMC_RISK_MANAGEMENT,
	SMC_PROCESS_TRANSACTION,
	SMC_DISPLAY_AUTHORIZATION,
	SMC_GET_AUTHORIZATION_SECURED_TAGS,
	SMC_GET_AUTHORIZATION_PLAIN_TAGS,
	SMC_DISPLAY_TRANSACTION_FINALIZATION,
	SMC_FINALIZE_TRANSACTION,
	SMC_GET_FINALIZATION_SECURED_TAGS,
	SMC_GET_FINALIZATION_PLAIN_TAGS,
	SMC_DISPLAY_REMOVE_CARD,
	SMC_REMOVE_CARD,

	/* NFC STATES*/
	NFC_DISPLAY_AUTHORIZATION,
	NFC_GET_AUTHORIZATION_SECURED_TAGS,
	NFC_GET_AUTHORIZATION_PLAIN_TAGS,
	NFC_SIMPLIFIED_ONLINE_PIN,
	NFC_DISPLAY_COMPLETE_TRANSACTION,
	NFC_COMPLETE_TRANSACTION,
	NFC_GET_FINALIZATION_SECURED_TAGS,
	NFC_GET_FINALIZATION_PLAIN_TAGS,
```
#### EMV Payment state machine

![Document sans titre (4)](https://user-images.githubusercontent.com/59020462/95754791-d6ed2380-0ca3-11eb-80be-0cb91394b9b8.jpg)

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
	public void cancel() {
		monitor.handleEvent(TaskEvent.CANCELLED);
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
	public void cancel() {
		monitor.handleEvent(TaskEvent.CANCELLED);
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
	public void cancel() {
		monitor.handleEvent(TaskEvent.CANCELLED);
	}
}
```

##### PaymentStatus
```java
    APPROVED,  // Transaction has been approved by terminal
    DECLINED, // Transaction has been declined by terminal
    CANCELLED, //Transaction has been cancelled by terminal or by application

    CARD_WAIT_FAILED,//Transaction has been failed because customer does not present a card and startNFCTransaction fail
    UNSUPPORTED_CARD, ///Transaction has been failed: Error returned by terminal, at contact transaction, when no application match between card and terminal's configuration

    NFC_OUTCOME_TRY_OTHER_INTERFACE, // Transaction has been failed: Error returned by terminal, at contactless transaction
    NFC_OUTCOME_END_APPLICATION,// Transaction has been failed: Error returned by terminal, at contactless transaction
    NFC_OUTCOME_FAILED,// Transaction has been failed: Error returned by terminal, at contactless transaction

    ERROR, // Transaction has been failed : when one of the tasks or commands has been fail
    ERROR_WRONG_ACTIVATED_READER, // Transaction has been failed : when terminal return wrong value in the tag DF70 at startNFCTransaction
    ERROR_MISSING_REQUIRED_CRYPTOGRAM,// Transaction has been failed :when the value of the tag 9f27 is wrong
    ERROR_WRONG_CRYPTOGRAM_VALUE, // Transaction has been failed : when in the response of the transaction process command the tag 9F27 is missing
    ERROR_WRONG_NFC_OUTCOME, // Transaction has been failed : when terminal returns wrong values in the nfc outcome byte array
}
```
#### Cancel Payment 
During the transaction, Customer may need to cancel process at any moment. You can use this code to cancel. You will receive onFinish() callback with paymentStatus cancelled. 
Note : If Payment state is Display result or Get level 1 or 2 logs, the transaction is already finish and cancel it is not possible. 

```java
            EMVPaymentStateMachine emvPaymentStateMachine = UCubeAPI.pay(...);
	    
	   ....
	   emvPaymentStateMachine.cancel();
```   

#### 6.4 MDM 

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
At the register process the SDK send the public certificate of terminal to the TMS, so the server can verifie the YouTransactor signature and then generate and return an SSL certificate unique by terminal. This SSL certificate is used to call the rest of web services.
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

The update can be done in two steps, check the TMS configuration and compare it with current versions this is performed by the `mdmCheckUpdate` method and then download the binary(ies) from TMS server and install them and this can be done by the `mdmUpdate` method.

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
		    List<BinaryUpdate> updateList = (List<BinaryUpdate>) params[0];
		    List<Config> cfgList = (List<Config>) params[1];

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

Once the connexionManager set and the device selected. You can call any RPC commands implemented in the SDK. This is the list of RPC Commands class: 

```java
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

All this commands are described in the terminal documentation. 

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
	private byte[] buffer; /* contains all the whole response of ucube without parsing */ 
	
}	
```
* Switch case of protection level, the parse of response will be different : 
	* In the case of none, it will be the same parse as Ready state, only `commandId, status & data` contain values.
	* In the case of signed, `commandId, status, data & data_mac` contain values. 
	* In the case of signed and ciphered, `commandId, status, data, data_mac & data_ciphered` contain values. 

Note that no MAC if the data is null.


![Cptr_logoYT](https://user-images.githubusercontent.com/59020462/71242500-663cdb00-230e-11ea-9a07-3ee5240c6a68.jpeg)
