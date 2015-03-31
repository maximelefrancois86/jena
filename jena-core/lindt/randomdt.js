Cardinal = lindt.registerDatatype( {
    uri: "lindt/randomdt.js#cardinal",
    isLegal: function(lexicalValue) {
        return lexicalValue==="one" || lexicalValue==="two";
    },
    equals: function(literal) {
        try {
            if(literal.uri==="lindt/randomdt.js#cardinal" && literal.lexicalValue===this.lexicalValue) {
                return true;
            }
            if(literal.uri==="http://www.w3.org/2001/XMLSchema#" && literal.lexicalValue==="1" && this.lexicalValue==="one") {
                return true;
            }
            if(literal.uri==="http://www.w3.org/2001/XMLSchema#" && literal.lexicalValue==="2" && this.lexicalValue==="two") {
                return true;
            }
        } catch(err) {
            return false;
        }
        return false;
    }
} );

Stupid = new lindt.Datatype( {
    uri: "lindt/randomdt.js#stupid",
    isValid: function() {
        return true;
    },
    equals: function(literal) {
        return true;
    }
} );


//one = new Cardinal("one");

