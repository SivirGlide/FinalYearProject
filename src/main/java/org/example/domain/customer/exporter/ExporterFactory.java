package org.example.domain.customer.exporter;

public class ExporterFactory {

    public static FileExporter getExporter(String filePath) {
        String extension = getExtension(filePath);

        return switch (extension.toLowerCase()) {
            case "csv"  -> new CsvExporter();
            case "json" -> new JsonExporter();
            // Future exporters — just add a new case:
            // case "xlsx" -> new ExcelExporter();
            // case "xml"  -> new XmlExporter();
            default -> throw new IllegalArgumentException("No exporter available for file type: " + extension);
        };
    }

    private static String getExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filePath.length() - 1) {
            throw new IllegalArgumentException("File has no extension: " + filePath);
        }
        return filePath.substring(dotIndex + 1);
    }
}