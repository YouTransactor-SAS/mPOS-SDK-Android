# uCube mPOS SDK Android
![Cptr_PlatformAPI](https://user-images.githubusercontent.com/59020462/71244593-2b897180-2313-11ea-95af-8a2fcce628eb.jpeg)

This repository provides a step by step documentation that will allow you to integrate our uCube library for Android SDK to develop your proper application. To do it just follows the instruction.

For more information about YouTransactor developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).


## I. Context
### 1 General overview 
YouTransactor mPOS products are : 
* uCube (with different models)
* uCube Touch

The uCube Touch is a new version of the uCube. There are some hardware differences, which are: 
* The uCube use the classical Bluetooth and the uCube Touch use the BLE 
* The uCube provide a magstripe reader but not the uCube Touch

The uCubeLib support these two product. There is a setup () API implemented by the uCubeLib which takes a YTMPOSProduct. It initializes the SDK to connect one of this two products. In this document “uCube” is used as a name for this two products.

#### 1.1 uCube
The uCube is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a Bluetooth connection to enable acceptance of magstripe, contactless and smart payment cards (depending on the model).

<p align="center">
  <img width="250" height="250" src="https://user-images.githubusercontent.com/59020462/76528252-cd32e180-6470-11ea-9182-742faca82167.png">
</p>

#### 1.2 uCube Touch
The uCube Touch is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a BLE connection to enable acceptance of contactless and smart payment cards.

<p align="center">
  <img width="300" height="350" src="https://user-images.githubusercontent.com/59020462/76528701-842f5d00-6471-11ea-9a56-579e172a57ac.png">
</p>

#### 1.3 Mobile Device
The mobile device can be either Android or iOS and typically hosts applications related to payment. It links the uCube / uCube Touch to the rest of the system.

The mobile device application consists of 2 modules:
* Business module
	* Application that meets the business needs of the end customer. This is for example a cashier    	    application in the case of a restaurant, or a control application in the case of transports.
* "uCubeLib" Module
	* Manages the Bluetooth connection with the uCube / uCube Touch
	* Drives the transaction between the uCube and the payment card.
	* Responsible for uCube software updates

The business module on the mobile device is developed by the integrator. It uses the user interfaces of the mobile device to fulfill the business needs of the customer. A sample application is provided to the integrator, under the SDK.

The uCubeLib module is developed by YouTransactor. It is delivered to the integrator as a library, and compiled with the business module to generate the payment application.

The purpose of this document is to describe the services provided by the uCubeLib module to the business module.

#### 1.4 The Management System
The management system is administered by YouTransactor and offers the following services:
* Management of the uCube fleet
* Deployment of software updates
* Deployment of payment parameters
* Other services

The management system does not require integration with the business module, so its operation is not developed in this documentation.

### 2 uCube management
#### 2.1 Setup 
##### 2.1.1 Initial configuration 
To be functional, in the scope of PCI PTS requirement, and SRED key shall be loaded securely in the device. This key is loaded locally by YouTransactor tools. The initial SALT is injected in the same way.

##### 2.1.2 Bleutooth pairing
Before using the payment function, the uCube must be paired with the mobile device via Bluetooth.

This pairing can be done by a "connect" method of the uCubeLib module. It will scan all available devices and will display a system pop-up prompting the user to select the device to use.

The uCubeLib needs the Bluetooth to be enabled, it will request to enable it, if it is disabled. And if the YTMPOSProduct chosen, when setup was called, was the uCube Touch, a Location permission will be requested.

<p align="center">
  <img width="370" height="600" src="https://user-images.githubusercontent.com/59020462/76528865-c5277180-6471-11ea-82e6-69320ed62dde.jpg">
</p>

#### 2.2 Switching uCube On/Off
The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the business module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The uCube Touch can be lights up exactly like the uCube, but also by using “connect” method. When connection established, the SDK check if the device is state, if it is power off, it turns it 

#### 2.3 Firmware update
During the life of the uCube, the uCube firmware could be updated (for bug fix, etc..). The “checkUpdate” method make a compare between current versions of firmware and configuration with These defined in TMS server. If the is a difference or a force update parameter is set to true, it returns the update list. The method “update” used to apply the updates, it takes as input the list of updates to be applied, it can be the output of “checkUpdate” method or a sub list of that. It downloads the binaries of each update in input list and install them all. 

#### 2.4 Send logs
uCubeLib provide a “LogManager” Class used to print logs in logcat at runtime, save these logs in files. And it needed, send these files to TMS server. Logs can be enabled or disabled. The “sendLogs” method is used to send a zip file that contain all saved logs files. 

## II. Technical Overview

### 1. General Architecture

This section describes the general uCube MPOS Android SDK architecture. The SDK provide: 

* Connexion module used to scan Bluetooth devices and pair then connect uCube (classical Bluetooth and BLE supported) 
* MDM module that implements all TMS Web services to manage uCube updates and debug.
* RPC module that implements all SVPP commands. This RPC APIs used to drive the SVPP for processing transactions or to update it.
* Payment module implements magstripe, contact and contactless transaction services. It uses the RPC module to call RPC commands during the transaction.
* UCubeAPI is the public interface exposed to the integrator it implements several APIs to mainly setup the connection with uCube, do a payment, check and start a uCube update, send logs to TMS server.

The Integrator is able to use the UCubeAPI interface and call RPC commands. 

![Cptr_Architecture](https://user-images.githubusercontent.com/59020462/71239040-d8f58880-2305-11ea-97d3-9441e2b7e0d3.jpeg)

### 2. Transaction Flow : Contact

![Cptr_TransactionSMC](https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg)


### 3. Transaction Flow : Contactless

![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)

### 4. Prerequisites

To embed the package that you need in your application, you have to be sure of certain things in your settings.
1. Received YouTransactor card terminal : uCube, uCubeTouch
2. The `minSDKVersion` must be at 21 to works properly.
3. The `targetSDKversion` 28 or later (as a consequence of the migration to AndroidX).
4. Following Google's best practices SDK 3.3.0 will migrate to AndroidX. For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependency

Our SDK is in the format “aar” in the library. So if you want to access to it here is what you must do.
You will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/ucube_lib.aar')

### 6. UCubeAPI
The APIs provided by UCubeAPI modules are:

```java
	initManagers (@Nonnull Context context)
		
	setup (@Nonnull Context context, @NonNull Activity activity, @NonNull YTMPOSProduct ytmposProduct, @Nonnull UCubeAPIListener uCubeAPIListener)
		
	YTMPOSProduct getYTMPOSProduct()
		
	connect (@Nonnull Activity activity, @NonNull UCubeInitListener uCubeInitListener)
		
	UCubeInfo getUCubeInfo()
	
	deletePairedUCube()
	
	pay(@Nonnull Context context, @Nonnull UCubePaymentRequest uCubePaymentRequest, @Nonnull UCubePaymentListener uCubePaymentListener)
	
	checkUpdate(@NonNull Activity activity, boolean forceUpdate, boolean checkOnlyFirmwareVersion, @Nonnull UCubeCheckUpdateListener uCubeCheckUpdateListener)

	update(@NonNull Activity activity, final @NonNull List<BinaryUpdate> updateList, @Nonnull UCubeAPIListener uCubeAPIListener)

	sendLogs(Activity activity, @Nonnull UCubeAPIListener uCubeAPIListener)
	
	close()
```

* You can use the sample app provided in this repository as a reference

#### initManagers (...)
* This API initializes the SDK by initializing differents modules; RPC, Payment, MDM…. It should be called in the begining, before calling any other API. 

```java
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.activity_main);
		   UCubeAPI.initManagers(getApplicationContext());
		...
```

#### setup (...)
* It takes in input the YTMPOSProduct that user of SDK chooses to use.
* It can throw two types of exception: BleNotSupportException and BluetoothNotSupportException.
* BleNotSupportException : mean that the YTMPOSProduct specified was the uCube_Touch and the used smartphone don’t support BLE.
* BluetoothNotSupportException : mean that the used smartphone doesn’t support Bluetooth.  
* It can throws an Exception if the `initManagers` API not already called.

```java
		try {
		   UCubeAPI.setup(getApplicationContext(), this, YTMPOSProduct.uCube, new UCubeAPIListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) {
		//TODO 
		       }
		       @Override
		       public void onFinish(boolean status) {
				 //TODO
		       }
		   });
		} catch (Exception e) {
		  e.printStackTrace();
		}
		...
```

#### getYTMPOSProduct ()
* This API returns the configured YTMPOSProduct if setup already called otherwise it returns null
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   ytmposProduct  = UCubeAPI.getYTMPOSProduct();
		   if (ytmposProduct != null) {
		       switch (ytmposProduct) {
			   case uCube:
				//TODO
			       break;
			   case uCube_touch:
				//TODO
			       break;
		       }
		   }
		} catch (Exception e) {
		   e.printStackTrace();
		}
		...
```

#### Connect (...)
* This API connect the paired uCube if there is already one otherwise it does a Bluetooth scan and the user should select one device. it connects it and save it. It registers the device in the MDM and get the MDM-CLIENT certificate of the device. To be used for the double-authentication when calling others MDM WS.
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   UCubeAPI.connect(this, new UCubeConnectListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) {
		//TODO
		       }
		       @Override
		       public void onFinish(boolean status, UCubeInfo uCubeInfo) {
		//TODO
		       }
		   });
		} catch (Exception e) {
		   e.printStackTrace();
		}
		….
```

#### getUCubeInfo ()
* This API returns an UCubeInfo which contains all paired uCube informations if there is already a paired one otherwise it returns null.
* It can throw an Exception if the “initManagers” method not already called.

```java
		UCubeInfo deviceInfos = null;
		try {
		   deviceInfos = UCubeAPI.getUCubeInfo();
		   //TODO
		} catch (Exception e) {
		   e.printStackTrace();
		}
		….
```

#### deletePairedUCube ()
* This API delete the current paired uCube if there is a saved one.
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   UCubeAPI.deletePairedUCube();
		} catch (Exception e) {
		   e.printStackTrace();
		   return;
		}
		….
```

#### Pay (...)
* This API activate all available reader in device and call Payment service and it depends from which reader is used to read card the specific service is called.
* This API takes in input a UCubePaymentRequest and gives in output a UCubePaymentResponse. 
* It makes a payment using the defined context in UCubePaymentRequest.
* During the payment this context will change and progress. At the end of the payment this context will be part of the UCubePaymentResponse + others attributes like payment state.
* It can throw an Exception if the “initManagers” method not already called.

##### UCubePaymentRequest

```java
		UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
		       .setAmount(amount)  // if amount not specified uCube will propose to enter the amount before start tx
		       .setCurrency(currency)  // CURRENCY_EUR or CURRENCY_USD or new Currency(iso_code, exponent, label) 
		       .setForceOnlinePin(true)
		       .setAuthorizationTask(new AuthorizationTask(this))  // Instance of class that implements  IAuthorizationTask.
		       .setRiskManagementTask(new RiskManagementTask(this))  // Instance of class that implements  IRiskManagementTask.
		       .setCardWaitTimeout(timeout) // in second exemple 30 means 30 seconds of timeout to wait card
		       .setTransactionType(trxType) // PURCHASE /  WITHDRAWAL  / REFUND /  PURCHASE_CASHBACK / MANUAL_CASH / INQUIRY
		       .setSystemFailureInfo(true)
		       .setSystemFailureInfo2(false)
		       .setPreferredLanguageList(Collections.singletonList("en"))
		       .setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)
		       .setRequestedSecuredTagList(Constants.TAG_TRACK2_EQU_DATA)
		       .setRequestedPlainTagList(Constants.TAG_MSR_BIN)
		       .build();
```

##### AuthorizationTask

```java
		public class AuthorizationTask implements IAuthorizationTask {
		  private byte[] authResponse;
		  private PaymentContext paymentContext;
		  @Override
		  public byte[] getAuthorizationResponse() {  return authResponse; }
		  @Override
		  public PaymentContext getContext() { return paymentContext;}
		  @Override
		  public void setContext(PaymentContext context) { this.paymentContext = context; }
		  @Override
		  public void execute(ITaskMonitor monitor) {
		   ...
		  }
		...
```

##### RiskManagementTask

```java
		public class RiskManagementTask implements IRiskManagementTask {
		  private PaymentContext paymentContext;
		  private byte[] tvr;
		  @Override
		  public byte[] getTVR() { return tvr; }
		  @Override
		  public PaymentContext getContext() {  return paymentContext; }
		  @Override
		  public void setContext(PaymentContext context) { this.paymentContext = context; }
		  @Override
		  public void execute(ITaskMonitor monitor) {
		   ...
		  }
		...
		}
```

##### Call API

```java
		try {
		 UCubeAPI.pay(this, paymentRequest, new UCubePaymentListener() {
		   @Override
		   public void onStart(byte[] ksn) {
		       Log.d(TAG, "KSN : " + Arrays.toString(ksn));
		       //TODO Send KSN to the acquirer server
		   }
		   @Override
		   public void onFinish(boolean status, UCubePaymentResponse uCubePaymentResponse) {
		if (status && uCubePaymentResponse != null) {
			   Log.d(TAG, "Payment status : " + uCubePaymentResponse.paymentState);
			   Log.d(TAG, "ucube name: " + uCubePaymentResponse.uCube.ucubeName);
			   Log.d(TAG, "ucube address: " + uCubePaymentResponse.uCube.ucubeAddress);
			   Log.d(TAG, "ucube part number: " + uCubePaymentResponse.uCube.ucubePartNumber);
			   Log.d(TAG, "card label: " + uCubePaymentResponse.cardLabel);
			   Log.d(TAG, "amount: " + uCubePaymentResponse.paymentContext.getAmount());
			   Log.d(TAG, "currency: " + uCubePaymentResponse.paymentContext.getCurrency().getLabel());
			   Log.d(TAG, "tx date: " + uCubePaymentResponse.paymentContext.getTransactionDate());
			   Log.d(TAG, "tx type: " + uCubePaymentResponse.paymentContext.getTransactionType().getLabel());
			   if (uCubePaymentResponse.paymentContext.getSelectedApplication() != null) {
			       Log.d(TAG, "app ID: " + uCubePaymentResponse.paymentContext.getSelectedApplication().getLabel());
			       Log.d(TAG, "app version: " + uCubePaymentResponse.paymentContext.getApplicationVersion());
			   }
			   Log.d(TAG, "system failure log1: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo()));
			   Log.d(TAG, "system failure log2: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo2()));
			   if (uCubePaymentResponse.paymentContext.getPlainTagTLV() != null)
			       for (Integer tag : uCubePaymentResponse.paymentContext.getPlainTagTLV().keySet())
					 Log.d(TAG, "Plain Tag : " + tag + " : " + bytesToHex(uCubePaymentResponse.paymentContext.getPlainTagTLV().get(tag)));
			   if (uCubePaymentResponse.paymentContext.getSecuredTagBlock() != null)
			       Log.d(TAG, "secure tag block: " + bytesToHex(uCubePaymentResponse.paymentContext.getSecuredTagBlock()));
		 }
			 }
		});
		} catch (Exception e) {   e.printStackTrace(); }

```

##### Response 
* Several response fields are available when the call back activity is called.
	* paymentContext 
	* uCube
	* cardLabel 

###### PaymentContext

```java
		PaymentState paymentStatus;
		EMVApplicationDescriptor selectedApplication;
		boolean allowFallback;
		int retryBeforeFallback;
		double amount;
		Currency currency;
		TransactionType transactionType;
		int applicationVersion;
		List<String> preferredLanguageList;
		byte[] uCubeInfos;
		byte[] ksn;
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
		byte[] transactionData;
		byte[] systemFailureInfo;
		byte[] systemFailureInfo2;
```

###### uCube

```java
		public class UCube {
		   public String ucubeName;
		   public String ucubeAddress;
		   public String ucubePartNumber;
		   public String ucubeSerialNumber;
		}
```

###### PaymentState
```java
		DEFAULT_INIT,
		GET_PN_ERROR,
		GET_MPOS_STATE_ERROR,
		TRANSACTION_MODE_ERROR,
		RISK_MANAGEMENT_TASK_NULL_ERROR,
		AUTHORIZATION_TASK_NULL_ERROR,
		DEVICE_TYPE_ERROR,
		NFC_MPOS_ERROR,
		CARD_WAIT_FAILED,
		CANCELLED,
		STARTED,
		ENTER_SECURE_SESSION,
		CARD_REMOVED,
		CHIP_REQUIRED,
		UNSUPPORTED_CARD,
		TRY_OTHER_INTERFACE,
		REFUSED_CARD,
		ERROR,
		AUTHORIZE,
		APPROVED,
		DECLINED
```

#### checkUpdate  (...)
* This API retrieve the information’s device then the device’s configuration on TMS server. It does a compare, and return a table of required Binary Updates. A binary update can be mandatory or not.

* This API takes those parameters: 
	* ForceUpdate: if true, update the same version will be accept
	* checkOnlyFirmwareVersion: if true, check updates of only firmware(s)

* It can throws an Exception if the initManagers() API not already called.

```java
		try {
			 UCubeAPI.checkUpdate(MainActivity.this,  forceUpdate, checkOnlyFirmwareVersion,
			   new UCubeCheckUpdateListener() {
			       @Override
			       public void onProgress(UCubeAPIState state) { }
			       @Override
			       public void onFinish(boolean status, List<BinaryUpdate> updateList, List<Config> cfgList) {  	//TODO
		}
			});
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### update (...)
* This API takes the list of Binary updates to be downloaded and installed.
* It downloads the binary Then it installs them sequentially
* After the install of SVPP firmware the device will reboot. 
* The install of the same version is not accepted by the SVPP.
* The downgrade is not accepted by the SVPP. 
* The forceUpdate of the same version is only accepted with the SVPP firmware is in Security OFF (only available for dev)
* It can throws an Exception if the initManagers() API not already called.

```java
		try {
		   UCubeAPI.update(activity, updateList,
		new UCubeAPIListener() {
			       @Override
			       public void onProgress(UCubeAPIState state) {
			       }
			       @Override
			       public void onFinish(boolean status) {
			       }
			});
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### sendLog ()
* uCube SDK manage a logcat that save all RPC exchanges and different user actions. 
* User of SDK can send this logs to be interpreted by the YouTransactor support team.
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   UCubeAPI.sendLogs(MainActivity.this, new UCubeAPIListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) { }
		       @Override
		       public void onFinish(boolean status) {  }
		   });
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### close ()
* This API is used to stop all managers and close connection with uCube.
* It can be called in the onDestroy () method of activity.

### 7. RPC : Call command


* This library allows the user to call differents RPC e.g. DisplayMessageWithoutKI, GetInfo, etc.
* User may want to call some RPC, it depends on implementation of one of the tasks “Application Selection Task”, “Risk Management Task” or “Authorization Task”.
* This is an example of DisplayMessageWithoutKI command call: 

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
