package it.mytest.esercizio.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import it.mytest.esercizio.entities.TransactionDetail;
import it.mytest.esercizio.entities.response.MoneyTransferResult;
import it.mytest.esercizio.error.InvalidDataException;

public interface IOperations {
	
	public ResponseEntity<Float> getBalance() throws InvalidDataException;
	
	public ResponseEntity<List<TransactionDetail>> getTransactionsList(String fromAccountingDate, String toAccountingDate) throws InvalidDataException;
	
	public ResponseEntity<MoneyTransferResult> moneyTransfer(String receiverName, String description, String currency, String amount, String executionDate) throws InvalidDataException;
}
