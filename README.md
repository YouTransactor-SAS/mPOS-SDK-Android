# uCube mPOS SDK Android

This repository provides a step by step documentation that will allow you to integrate our uCube library for Android SDK to develop your proper application. To do it just follows the instruction.
For more information about SumUp developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).

## Prerequisites

To embed the package that you need in your application, you have to be sure of certain things in your settings.
1. Received YouTransactor card terminal : uCube, uCubeTouch
2. Your SDK Version must be at 21 to works properly.
3. The TargetSDKversion 28 or later.
4. Following Google's best practices SDK 3.3.0 will migrate to AndroidX. For more information about AndroidX and how to migrate see Google AndroidX Documentation


## Dependency

Our SDK is in the format “aar” in the library. So if you want to access to it here is what you must do.
You will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/ucube_lib.aar')


## General Architecture


![Cptr_Architecture](https://user-images.githubusercontent.com/59020462/71239040-d8f58880-2305-11ea-97d3-9441e2b7e0d3.jpeg)


## Transaction Flow : SMC


![Cptr_TransactionSMC](https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg)


## Transaction Flow : NFC


![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)



## UCubeAPI : Initialization

* This API initializes the sdk by initializing differents modules; RPC, Payment, MDM…
* It takes in input the YTMPOSProduct that user of SDK choose to use.
* It can throws two type of exception : BleNotSupportException and BluetoothNotSupportException.
* BleNotSupportException : mean that the YTMPOSProduct specified was the uCube_Touch and the used smartphone don’t supports BLE.
* BluetoothNotSupportException : mean that the used smartphone don’t support Bluetooth. 
* User should call this API before start using any other API of SDK. 

	
		
		try {
			UCubeAPI.init(getApplicationContext(), activity, YTMPOSProduct.uCube_touch);
		} catch (BleNotSupportException e) {
	  		e.printStackTrace();
		} catch (BluetoothNotSupportException e) {
 	  		e.printStackTrace();
		}
		




## UCubeAPI : Payment

#### Payment request
* This API start a payment by activating available readers in the device. (NFC, SMC, MSR)
* It take in input a UCubePayRequest and give in output a UCubePayResponse.
* The payment params that the user should specify are :
	- [ ] **Amount**
	- [ ] **Currency**  // CURRENCY_EUR or CURRENCY_USD or new Currency(iso_code, exponent, label) 
	- [ ] **Transaction type** // PURCHASE /  WITHDRAWAL  / REFUND /  PURCHASE_CASHBACK / MANUAL_CASH / INQUIRY
	- [ ] **Card wait timeout**
	- [ ] **Application selection Task** // Instance of class which implements IApplicationSelectionTask, if null SDK will use the default ApplicationSelectionTask.
	- [ ] **Authorization task** // Instance of class that implements  IAuthorizationTask.
	- [ ] **RiskManagement task** // Instance of class that implements  IRiskManagementTask.


			UCubePayRequest paymentRequest = new UCubePayRequest.Builder(
				1.0, 
				UCubePayRequest.CURRENCY_EUR,
				TransactionType.PURCHASE, 
				UCubePayRequest.DEFAULT_CARD_WAIT_TIMEOUT,
				new AuthorizationTask(this), 
				new RiskManagementTask(this)
			) .build();

			UCubeAPI.pay(activity, paymentRequest,  PAYMENT_REQUEST_CODE)



#### Example of Application selection task

		public class MyApplicationSelectionTask implements IApplicationSelectionTask {

  			private List<EMVApplicationDescriptor> applicationList;
  			private List<EMVApplicationDescriptor> candidateList;
  			private PaymentContext context;

			@Override
			public void setAvailableApplication(List<EMVApplicationDescriptor> applicationList) {   this.applicationList = applicationList; }

			@Override
			public List<EMVApplicationDescriptor> getSelection() { return candidateList;  }

			@Override
			public PaymentContext getContext() {  return context;  }

			@Override
			public void setContext(PaymentContext paymentContext) {  this.context = paymentContext; }
			 
			@Override
			public void execute(ITaskMonitor monitor) {
			    candidateList = new ArrayList<>();

			    // todo

			    monitor.handleEvent(TaskEvent.SUCCESS);
  			}
		}



#### Example of Risk Managament task

		public class MyRiskManagementTask implements IRiskManagementTask {

  			private ITaskMonitor monitor;
  			private PaymentContext paymentContext;
  			private byte[] tvr;

  			@Override
  			public byte[] getTVR() {  return tvr; }

  			@Override
  			public PaymentContext getContext() {  return paymentContext; }

  			@Override
  			public void setContext(PaymentContext context) {  this.paymentContext = context; }

  			@Override
  			public void execute(ITaskMonitor monitor) {
     			this.monitor = monitor;
    
        			// todo

     			monitor.handleEvent(TaskEvent.SUCCESS);
  			}
		}



#### Example of Authorization task

		public class MyAuthorizationTask implements IAuthorizationTask {

  			private byte[] authResponse;
  			private ITaskMonitor monitor;
  			private PaymentContext paymentContext;

  			@Override
  			public byte[] getAuthorizationResponse() {   return authResponse;  }

  			@Override
  			public PaymentContext getContext() {   return paymentContext; }

 			@Override
  			public void setContext(PaymentContext context) { this.paymentContext = context;  }

  			@Override
  			public void execute(ITaskMonitor monitor) {
     			this.monitor = monitor;

      			//TODO
  			}
		}



#### Additional info :

The UCubePayRequest has an optional attribute that can be used to add list of tags.
The content of this tags will be returned in the response as UCubePayResponse.requestedTags attribute : byte[ ].

		UCubePayRequest paymentRequest = new UCubePayRequest.Builder(...)
		.setRequestTags(tags)
		.build();

		UCubeAPI.pay(activity, paymentRequest,  PAYMENT_REQUEST_CODE);



#### Payment response

Several response fields are available when the callback activity is called. 

- [ ] TxStatus : Possible Values :
	* DEFAULT_INIT
	* NFC_MPOS_ERROR
	* CARD_WAIT_FAILED
	* CANCELLED
	* STARTED
	* CARD_REMOVED
	* CHIP_REQUIRED
	* UNSUPPORTED_CARD
	* TRY_OTHER_INTERFACE
	* REFUSED_CARD
	* ERROR
	* AUTHORIZED
	* APPROVED
	* DECLINED

- [ ] TxCode : int

- [ ] TxInfo : Transaction info object containing informaation about this transaction. It contains the following information:
	* ProductInfomation : ProductInfo
	* AppVersion : String
	* TxDate : Date
	* TxAmount : double
	* TxCurrency : Currency
	* TxType : TransactionType
	* EntryMode : e.g. CHIP
	* NumberOfInstallments : int
	* CardType : e.g. MASTERCARD
	* Last for digits of the card : String
	* SelectedApplication : EMVApplicationDescriptor



#### Handle Payment result

		@Override
		protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
   			super.onActivityResult(requestCode, resultCode, data);

   			if( requestCode == PAYMENT_REQUEST_CODE)
   			// TODO parse response
		}




## UCubeAPI : Update + send Logs


* The update API registers the device in YT MDM then it retrive the current svpp version and check if it is different from configured version or not. If it is different an update process is executed.
* It takes ForceUpdate : boolean as a param. If this param true the API will update the svpp even if it has the same version as the configured one.
		
		UCubeAPI.update(forceUpdate);



* uCube SDK manage a logback that save all RPC exchanges and differents user actions.
* User of SDK can send logs to be interpreted by the support team using this API : 
		
		UCubeAPI.sendLogs();




## RPC : Call command


* This library allows user to call differents RPC e.g. DisplayMessageWithoutKI, GetInfo, etc.
* User may want to call some RPC, it depends of implementation of one of the tasks “Application Selection Task”, “Risk Management Task” or “Authorization Task”.
* This is an example of DisplayMessageWithoutKI command call : 

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


