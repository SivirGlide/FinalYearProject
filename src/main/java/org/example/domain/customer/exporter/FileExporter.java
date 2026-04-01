package org.example.domain.customer.exporter;

import org.example.domain.customer.reader.DataSet;

public interface FileExporter {
    /**
     * Takes a DataSet and writes it to the given file path.
     * Any new exporter (Excel, XML, etc.) just needs to implement this one method.
     */
    void exportFile(DataSet dataSet, String filePath) throws Exception;
}