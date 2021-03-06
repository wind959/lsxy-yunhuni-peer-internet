/*
 * An XML document type.
 * Localname: getreply
 * Namespace: urn:SY_SoapServer
 * Java type: sy_soapserver.GetreplyDocument
 *
 * Automatically generated - do not modify.
 */
package sy_soapserver;


/**
 * A document containing one getreply(@urn:SY_SoapServer) element.
 *
 * This is a complex type.
 */
public interface GetreplyDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(GetreplyDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4262BC6C064011807FED7385999847C2").resolveHandle("getreply574bdoctype");
    
    /**
     * Gets the "getreply" element
     */
    sy_soapserver.GetreplyDocument.Getreply getGetreply();
    
    /**
     * Sets the "getreply" element
     */
    void setGetreply(sy_soapserver.GetreplyDocument.Getreply getreply);
    
    /**
     * Appends and returns a new empty "getreply" element
     */
    sy_soapserver.GetreplyDocument.Getreply addNewGetreply();
    
    /**
     * An XML getreply(@urn:SY_SoapServer).
     *
     * This is a complex type.
     */
    public interface Getreply extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Getreply.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4262BC6C064011807FED7385999847C2").resolveHandle("getreply435belemtype");
        
        /**
         * Gets the "user" element
         */
        java.lang.String getUser();
        
        /**
         * Gets (as xml) the "user" element
         */
        org.apache.xmlbeans.XmlString xgetUser();
        
        /**
         * Tests for nil "user" element
         */
        boolean isNilUser();
        
        /**
         * Sets the "user" element
         */
        void setUser(java.lang.String user);
        
        /**
         * Sets (as xml) the "user" element
         */
        void xsetUser(org.apache.xmlbeans.XmlString user);
        
        /**
         * Nils the "user" element
         */
        void setNilUser();
        
        /**
         * Gets the "passwd" element
         */
        java.lang.String getPasswd();
        
        /**
         * Gets (as xml) the "passwd" element
         */
        org.apache.xmlbeans.XmlString xgetPasswd();
        
        /**
         * Tests for nil "passwd" element
         */
        boolean isNilPasswd();
        
        /**
         * Sets the "passwd" element
         */
        void setPasswd(java.lang.String passwd);
        
        /**
         * Sets (as xml) the "passwd" element
         */
        void xsetPasswd(org.apache.xmlbeans.XmlString passwd);
        
        /**
         * Nils the "passwd" element
         */
        void setNilPasswd();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static sy_soapserver.GetreplyDocument.Getreply newInstance() {
              return (sy_soapserver.GetreplyDocument.Getreply) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static sy_soapserver.GetreplyDocument.Getreply newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (sy_soapserver.GetreplyDocument.Getreply) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static sy_soapserver.GetreplyDocument newInstance() {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static sy_soapserver.GetreplyDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static sy_soapserver.GetreplyDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static sy_soapserver.GetreplyDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static sy_soapserver.GetreplyDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static sy_soapserver.GetreplyDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static sy_soapserver.GetreplyDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static sy_soapserver.GetreplyDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static sy_soapserver.GetreplyDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (sy_soapserver.GetreplyDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
