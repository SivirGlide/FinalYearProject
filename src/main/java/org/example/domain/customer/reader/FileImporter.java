package org.example.domain.customer.reader;

public interface FileImporter {
    /**
     * Takes a file path and returns a DataSet ready for validation/transformation.
     * Any new importer (Excel, XML, etc.) just needs to implement this one method.
     */
    DataSet importFile(String filePath) throws Exception;
}
