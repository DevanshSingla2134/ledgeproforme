package com.ledgepro.exception;

public class DuplicateInvoiceException extends LedgeProException {
    public DuplicateInvoiceException(String invoiceNumber) {
        super("DUPLICATE_INVOICE", "Invoice with number '" + invoiceNumber + "' already exists. Duplicate issuance prevented.");
    }
}
