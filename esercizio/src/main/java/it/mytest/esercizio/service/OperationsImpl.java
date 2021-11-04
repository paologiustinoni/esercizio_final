package it.mytest.esercizio.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.mytest.esercizio.controller.AccountController;
import it.mytest.esercizio.entities.Account;
import it.mytest.esercizio.entities.Address;
import it.mytest.esercizio.entities.Balance;
import it.mytest.esercizio.entities.Creditor;
import it.mytest.esercizio.entities.LegalPersonBeneficiary;
import it.mytest.esercizio.entities.MoneyTransfer;
import it.mytest.esercizio.entities.NaturalPersonBeneficiary;
import it.mytest.esercizio.entities.TaxRelief;
import it.mytest.esercizio.entities.Transaction;
import it.mytest.esercizio.entities.TransactionDetail;
import it.mytest.esercizio.entities.TransactionDetails;
import it.mytest.esercizio.entities.response.MoneyTransferResult;
import it.mytest.esercizio.error.InvalidDataException;
import it.mytest.esercizio.error.Payload;
import it.mytest.esercizio.util.DateUtils;
import it.mytest.esercizio.util.HeaderAndURIUtils;

@Service
public class OperationsImpl implements IOperations {

	private static Logger LOGGER = LoggerFactory.getLogger(OperationsImpl.class);
	
	final String sandboxUrl = "https://sandbox.platfr.io";
	
	private float INVALID_VALUE = -99999;
	
	@Value("${accountId}")
	String accountId;


	private InvalidDataException createInvalidDataException(String message) {
		it.mytest.esercizio.error.Error error = new it.mytest.esercizio.error.Error();
		error.setCode("REQ017");
		error.setDescription(message);
		error.setParams("");
		return new InvalidDataException("KO", Collections.singletonList(error), new Payload());
	}
	
	@Override
	public ResponseEntity<Float> getBalance() throws InvalidDataException {
		//float ret = INVALID_VALUE;
    	
    	RestTemplate restTemplate = new RestTemplate();
    	
    	String balanceUrl = "/api/gbs/banking/v4.0/accounts/"+ accountId + "/balance";
    	String baseUrl = sandboxUrl + balanceUrl;
    	URI uri;
		try {
			uri = new URI(baseUrl);
			
	    	HttpHeaders headers = HeaderAndURIUtils.composeHeader();
	    	HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
	    	ResponseEntity<Balance> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Balance.class);
    	
	    	//ret = result.getBody().getPayload().getAvailableBalance();
	    	return ResponseEntity.ok().body(result.getBody().getPayload().getAvailableBalance());
	    	     
		} catch (URISyntaxException e) {
			LOGGER.error("{}, {}", e, e.getMessage());
			
			throw createInvalidDataException(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<List<TransactionDetail>> getTransactionsList(String fromAccountingDate, String toAccountingDate) throws InvalidDataException {
		String msg;
    	if (fromAccountingDate.isEmpty() || toAccountingDate.isEmpty()) {
    		msg = "Invalid date/s specified";
    		LOGGER.error(msg);
    		
    		throw createInvalidDataException(msg);
    	}
    			
    	boolean fromValid = DateUtils.isDateValid(fromAccountingDate);
    	boolean toValid = DateUtils.isDateValid(toAccountingDate);
    	boolean isValidInterval = DateUtils.isValidInterval(fromAccountingDate, toAccountingDate);
    	
    	if (!fromValid || !toValid || !isValidInterval) {
       		msg = "Invalid fromAccountingDate or toAccountingDate specified";
    		LOGGER.error(msg);
    		
    		throw createInvalidDataException(msg);
    	}
    	
    	List<TransactionDetail> listTransactionDetails = null;
    	
    	if (fromValid && toValid && isValidInterval) {
            
            String transactionListUrl = "/api/gbs/banking/v4.0/accounts/"+  accountId + "/transactions";
        	final String baseUrl = sandboxUrl + HeaderAndURIUtils.composeTransaciontUrl(transactionListUrl, fromAccountingDate, toAccountingDate);
        	URI uri;
        	
        	try {
    			uri = new URI(baseUrl);
    			RestTemplate restTemplate = new RestTemplate();
    	    	HttpHeaders headers = HeaderAndURIUtils.composeHeader();
    	    	HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
    	    	ResponseEntity<Transaction> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Transaction.class);

    	    	// check status code value return in order to avoid some great fright to user..
    	    	// can you image if the user sees a negative value from his app???	    	
    	    	int statusCodeValue = result.getStatusCodeValue();
    	    	
    	    	listTransactionDetails = null;
    	    	TransactionDetails transactionDetails = result.getBody().getPayload();
    	    	if (transactionDetails != null) {
    	    		listTransactionDetails = transactionDetails.getList();
    	    	}
    		} catch (URISyntaxException e) {
    			LOGGER.error("{}, {}", e, e.getMessage());
    			
    			throw createInvalidDataException(e.getMessage());
    		}
        	
    	} 
    	
    	return new ResponseEntity<List<TransactionDetail>>(listTransactionDetails, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<MoneyTransferResult> moneyTransfer(String receiverName, String description, String currency,
			String amount, String executionDate) throws InvalidDataException {
		String msg;
    	if (receiverName.isEmpty() || description.isEmpty() || currency.isEmpty() || amount.isEmpty() || executionDate.isEmpty()) {
    		msg = "Some data specified are empty";
    		LOGGER.error(msg);
    		
    		throw createInvalidDataException(msg);
    	}
    	
    	boolean executionDateValid = DateUtils.isDateValid(executionDate);

		if (!executionDateValid) {
			msg = "Invalid executionDate date specified";
			LOGGER.error(msg);
			
			throw createInvalidDataException(msg);
		}

		if (Float.parseFloat(amount) <= 0) {
			msg = "Invalid amount specified";
			LOGGER.error(msg);
			
			throw createInvalidDataException(msg);
		}
		
		RestTemplate restTemplate = new RestTemplate();

		String moneyTransferUrl = "/api/gbs/banking/v4.0/accounts/" + accountId + "/payments/money-transfers";
		final String baseUrl = sandboxUrl + moneyTransferUrl;

		URI uri;
		try {

			ObjectMapper mapper = new ObjectMapper();

			uri = new URI(baseUrl);

			HttpHeaders headers = HeaderAndURIUtils.composeHeaderForPost();

			MoneyTransfer example = null;
			example = createAndCompileMoneyTransferIncorrect(receiverName, description, currency, amount, executionDate);
			// decomment this line in order to get aperfectly working transaction..
			//example = createAndCompileMoneyTransferCorret(receiverName, description, currency, amount, executionDate);
			String jsonString = mapper.writeValueAsString(example);

			HttpEntity<String> requestEntity = new HttpEntity<String>(jsonString, headers);
			//ResponseEntity<MoneyTransferResult> result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, MoneyTransferResult.class);
			ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
			
			// convert result to moneyTransferResult
			if (HttpStatus.OK == result.getStatusCode()) {
				String body = result.getBody();
				JSONObject obj = new JSONObject(body);
				String payload = obj.get("payload").toString();
				MoneyTransferResult moneyTransferResult = mapper.readValue(payload, MoneyTransferResult.class);
				
				LOGGER.debug("moneyTransferResult={}", moneyTransferResult);
	
				return ResponseEntity.ok().body(moneyTransferResult);
			} else { 
				return null;
			}
		} catch (URISyntaxException e) {
			LOGGER.error("{}, {}", e, e.getMessage());
    		throw createInvalidDataException(e.getMessage());
		} catch (JsonProcessingException e1) {
			LOGGER.error("{}, {}", e1, e1.getMessage());
			throw createInvalidDataException(e1.getMessage());
		} catch (JSONException e1) {
			LOGGER.error("{}, {}", e1, e1.getMessage());
			throw createInvalidDataException(e1.getMessage());
		} 
	}

	private MoneyTransfer createAndCompileMoneyTransferCorret(String receiverName, String description, String currency, String amount, String executionDate) {
		MoneyTransfer transfer = new MoneyTransfer();
       	Creditor creditor = new Creditor();
    	creditor.setName(receiverName);
    	
    	Account account = new Account();
    	account.setAccountCode("IT60X0542811101000000123456");
    	account.setBicCode("SELBIT2BXXX");
    	 	
    	creditor.setAccount(account);    	
    	transfer.setCreditor(creditor);
    	
    	transfer.setExecutionDate(executionDate);
    	transfer.setUri("REMITTANCE_INFORMATION");
    	transfer.setDescription(description);
    	transfer.setAmount(Float.parseFloat(amount));
    	transfer.setCurrency(currency);

    	transfer.setIsUrgent(false);
    	transfer.setIsInstant(false);
    	transfer.setFeeType("SHA");
    	transfer.setFeeAccountId(accountId);
		
		NaturalPersonBeneficiary naturalPersonBeneficiary = new NaturalPersonBeneficiary();
		naturalPersonBeneficiary.setFiscalCode1("GSTPLA73B22A794Y");
		
		LegalPersonBeneficiary legalPersonBeneficiary = new LegalPersonBeneficiary();
		legalPersonBeneficiary.setFiscalCode("FISCAL_CODE");
		legalPersonBeneficiary.setLegalRepresentativeFiscalCode("FISCAL_CODE");
		
		TaxRelief taxRelief = new TaxRelief();
		taxRelief.setBeneficiaryType("NATURAL_PERSON");
		taxRelief.setCreditorFiscalCode("BNCPLA96S02A794M");
		taxRelief.setIsCondoUpgrade(false);
		taxRelief.setTaxReliefId("L449");
		taxRelief.setNaturalPersonBeneficiary(naturalPersonBeneficiary);
		taxRelief.setLegalPersonBeneficiary(legalPersonBeneficiary);
		
		transfer.setTaxRelief(taxRelief);
    	
    	return transfer;
	}
	
    private MoneyTransfer createAndCompileMoneyTransferIncorrect(String receiverName, String description, String currency, String amount, String executionDate) {   
    	LOGGER.debug("createAndCompileMoneyTransfer called...");
    	
    	MoneyTransfer transfer = new MoneyTransfer();
       	Creditor creditor = new Creditor();
    	creditor.setName(receiverName);
    	
    	Account account = new Account();
    	account.setAccountCode("IT23A0336844430152923804660");
    	account.setBicCode("SELBIT2BXXX");
    	
    	Address address = new Address();
    	address.setAddress("My address");
    	address.setCity("My city");
    	address.setCountryCode("My country code");
    	
    	creditor.setAccount(account);
    	creditor.setAddress(address);
    	
    	transfer.setCreditor(creditor);
    	
    	transfer.setExecutionDate(executionDate);
    	transfer.setUri("REMITTANCE_INFORMATION");
    	transfer.setDescription(description);
    	transfer.setAmount(Float.parseFloat(amount));
    	transfer.setCurrency(currency);

    	transfer.setIsUrgent(false);
    	transfer.setIsInstant(false);
    	transfer.setFeeType("SHA");
    	transfer.setFeeAccountId(accountId);
		
		NaturalPersonBeneficiary naturalPersonBeneficiary = new NaturalPersonBeneficiary();
		naturalPersonBeneficiary.setFiscalCode1("MRLFNC81L04A859L");
		
		LegalPersonBeneficiary legalPersonBeneficiary = new LegalPersonBeneficiary();
		legalPersonBeneficiary.setFiscalCode("FISCAL_CODE");
		legalPersonBeneficiary.setLegalRepresentativeFiscalCode("FISCAL_CODE");
		
		TaxRelief taxRelief = new TaxRelief();
		taxRelief.setBeneficiaryType("NATURAL_PERSON");
		taxRelief.setCreditorFiscalCode("56258745832");
		taxRelief.setIsCondoUpgrade(false);
		taxRelief.setTaxReliefId("L449");
		taxRelief.setNaturalPersonBeneficiary(naturalPersonBeneficiary);
		taxRelief.setLegalPersonBeneficiary(legalPersonBeneficiary);
		
		transfer.setTaxRelief(taxRelief);
    	
    	return transfer;
    }
	
}
