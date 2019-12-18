# uCube mPOS SDK Android

## General Architecture

## Transaction Flow : SMC

## Transaction Flow : NFC

## UCubeAPI : Initialization
------
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
		


------
[www.youtransactor.com](https://www.youtransactor.com)

## UCubeAPI : Payment
------
#### Payment request
* This API start a payment by activating available readers in the device. (NFC, SMC, MSR)
* It take in input a UCubePayRequest and give in output a UCubePayResponse.
* The payment params that the user should specify are :
	- [ ] Amount
	- [ ] Currency  // CURRENCY_EUR or CURRENCY_USD or new Currency(iso_code, exponent, label) 
	- [ ] Transaction type // PURCHASE /  WITHDRAWAL  / REFUND /  PURCHASE_CASHBACK / MANUAL_CASH / INQUIRY
	- [ ] Card wait timeout
	- [ ] Application selection Task // Instance of class which implements IApplicationSelectionTask, if null SDK will use the default ApplicationSelectionTask.
	- [ ] Authorization task // Instance of class that implements  IAuthorizationTask.
	- [ ] RiskManagement task // Instance of class that implements  IRiskManagementTask.


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


------
## UCubeAPI : Update + send Logs

## RPC : Call command