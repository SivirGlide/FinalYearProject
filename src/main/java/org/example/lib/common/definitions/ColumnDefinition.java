package org.example.lib.common.definitions;

import java.util.Set;

public class ColumnDefinition {

    public enum ColumnType {
        STRING,         // Plain text
        DATE,           // java.time.LocalDate
        STRING_ARRAY    // String[] — used for lists of ISO codes, etc.
    }

    private final String columnName;
    private final ColumnType expectedType;
    private final boolean required;           // If true, column MUST be present
    private final boolean nullsAllowed;       // If false, any null value is flagged
    private final int exactLength;            // -1 means "no length constraint"
    private final Set<String> allowedValues;  // Empty set means "no constraint"
    private final String comment;             // Human-readable note (mirrors the schema comment)

    private ColumnDefinition(Builder builder) {
        this.columnName    = builder.columnName;
        this.expectedType  = builder.expectedType;
        this.required      = builder.required;
        this.nullsAllowed  = builder.nullsAllowed;
        this.exactLength   = builder.exactLength;
        this.allowedValues = builder.allowedValues;
        this.comment       = builder.comment;
    }

    // -------------------------------------------------------------------------
    // Getters — validator reads these to know what rules to apply
    // -------------------------------------------------------------------------

    public String      getColumnName()    { return columnName;    }
    public ColumnType  getExpectedType()  { return expectedType;  }
    public boolean     isRequired()       { return required;      }
    public boolean     isNullsAllowed()   { return nullsAllowed;  }
    public int         getExactLength()   { return exactLength;   }
    public Set<String> getAllowedValues() { return allowedValues; }
    public String      getComment()       { return comment;       }

    // -------------------------------------------------------------------------
    // Builder pattern — lets us construct ColumnDefinitions in a readable way:
    //   new ColumnDefinition.Builder("Nationality", STRING).exactLength(2).build()
    //
    // A "Builder" is just a helper object that collects all the optional settings
    // before creating the final immutable ColumnDefinition.
    // -------------------------------------------------------------------------

    public static class Builder {
        // Required fields set in constructor
        private final String columnName;
        private final ColumnType expectedType;

        // Optional fields with sensible defaults
        private boolean required      = true;
        private boolean nullsAllowed  = false;
        private int exactLength       = -1;
        private Set<String> allowedValues = Set.of(); // empty = no constraint
        private String comment        = "";

        public Builder(String columnName, ColumnType expectedType) {
            this.columnName   = columnName;
            this.expectedType = expectedType;
        }

        public Builder required(boolean required)           { this.required = required;           return this; }
        public Builder nullsAllowed(boolean nullsAllowed)   { this.nullsAllowed = nullsAllowed;   return this; }
        public Builder exactLength(int exactLength)         { this.exactLength = exactLength;     return this; }
        public Builder allowedValues(Set<String> values)   { this.allowedValues = values;        return this; }
        public Builder comment(String comment)              { this.comment = comment;             return this; }

        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
