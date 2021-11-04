package it.mytest.esercizio.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.validator.GenericValidator;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.mytest.esercizio.db.Transactions;
import it.mytest.esercizio.db.TransactionsRepository;
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
import it.mytest.esercizio.service.IOperations;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/account")
public class AccountController {
	
	private static Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
	
	@Autowired
	IOperations operationService;
	
	@Value("${accountId}")
	String accountId;
	
	@Autowired
	TransactionsRepository transactionRepository;
	
	
    @GetMapping("/balance")
    public ResponseEntity<Float> getBalance() throws InvalidDataException {
    	LOGGER.debug("accountId={}", accountId);
    	
    	saveData("balance");

    	return operationService.getBalance();
    }    
    
    
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDetail>> getTransactionsList(@RequestParam String fromAccountingDate,
    								  				   @RequestParam String toAccountingDate) throws InvalidDataException {
    	LOGGER.debug("accountId={}, fromAccountingDate={}, toAccountingDate={}", accountId, fromAccountingDate, toAccountingDate);
    	
    	saveData("transactionList");
    	
    	return operationService.getTransactionsList(fromAccountingDate, toAccountingDate);
    }
    

	@RequestMapping(method = RequestMethod.POST, path = "/moneyTransfer")
	public ResponseEntity<MoneyTransferResult> moneyTransfer(@RequestParam String receiverName, 
							  @RequestParam String description,
							  @RequestParam String currency,
							  @RequestParam String amount,
							  @RequestParam String executionDate) throws InvalidDataException {
    	LOGGER.debug("receiverName={}, description={}, currency={}, amount={}, executionDate={}", receiverName, description, currency, amount, executionDate);
    	
    	saveData("moneyTransfer");
    	
    	return operationService.moneyTransfer(receiverName, description, currency, amount, executionDate);
    }
    
	
    
    private String getCurrentDateTime() {
    	SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss");
    	Date date = new Date(System.currentTimeMillis());
    	return formatter.format(date);
    }
    
	private void saveData(String operation) {
		LOGGER.debug("Saving data to db...");
		
		Transactions t = new Transactions();
		
    	t.setTransactionKey(UUID.randomUUID().toString());
    	t.setDateTime(getCurrentDateTime());
    	t.setAccount(accountId);
    	t.setOperation(operation);
    	
    	transactionRepository.save(t);
	}
	
}
