package de.tuberlin.tfdacmacs.client.attribute.exceptions;


public class InvalidAttributeValueIdentifierException extends RuntimeException {
    public InvalidAttributeValueIdentifierException(String attributeValueId) {
        super("Could not find attribute value: " + attributeValueId);
    }
}
