var lindt = new function() {
    var datatypes = {};
    this.getDatatype = function(uri) {
        return datatypes[uri];
    };
    this.registerDatatype = function(o) {
        if(typeof o === "undefined") {
            throw new Error("The construction of a Datatype requires a parameter.");
        }
        if(!o.hasOwnProperty("uri") || typeof o.uri !== "string") {
            throw new Error("The Datatype construction parameter must contain a string property 'uri'.");
        }
        if(!o.hasOwnProperty("isLegal") || typeof o.isLegal!== "function" || o.isValid.length!==1) {
            throw new Error("The Datatype construction parameter must contain a function property 'isLegal' with one parameter.");
        }
        if(!o.hasOwnProperty("equals") || typeof o.equals!== "function"  || o.equals.length!==1) {
            throw new Error("The Datatype construction parameter must contain a function property 'equals' with one parameter.");
        }
        if(datatypes.hasOwnProperty(o.uri)) {
            throw new Error("A Datatype with uri "+o.uri+" already exists");
        }
        dt = function(lexicalValue) {
            if(!dt.isLegal(lexicalValue)) {
                throw new Error("lexical Value " + lexicalValue + " is not legal for datatype " + dt.uri);
            }
            this.lexicalValue = lexicalValue;
        };
        dt.prototype.datatypeURI = o.uri;
        dt.prototype.isLegal = o.isLegal;
        dt.prototype.equals = o.equals;
        datatypes[o.uri] = dt;
        return dt;
    };
};

