package org.example.lib.common.columns;

import java.util.Set;

public class ColumnDefinition {


    private final String columnName;
    private final ColumnType expectedType;
    private final boolean required;
    private final boolean nullsAllowed;
    private final int exactLength;            // -1 means "no length constraint"
    private final Set<String> allowedValues;  // Empty set means "no constraint"
    private final String comment;

    private ColumnDefinition(Builder builder) {
        this.columnName    = builder.columnName;
        this.expectedType  = builder.expectedType;
        this.required      = builder.required;
        this.nullsAllowed  = builder.nullsAllowed;
        this.exactLength   = builder.exactLength;
        this.allowedValues = builder.allowedValues;
        this.comment       = builder.comment;
    }

    public String      getColumnName()    { return columnName;    }
    public ColumnType  getExpectedType()  { return expectedType;  }
    public boolean     isRequired()       { return required;      }
    public boolean     isNullsAllowed()   { return nullsAllowed;  }
    public int         getExactLength()   { return exactLength;   }
    public Set<String> getAllowedValues() { return allowedValues; }
    public String      getComment()       { return comment;       }

    public static class Builder {

        private final String columnName;
        private final ColumnType expectedType;

        private boolean required      = true;
        private boolean nullsAllowed  = false;
        private int exactLength       = -1;
        private Set<String> allowedValues = Set.of();
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
