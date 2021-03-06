/*
 * An XML document type.
 * Localname: sendmsg
 * Namespace: urn:SY_SoapServer
 * Java type: sy_soapserver.SendmsgDocument
 *
 * Automatically generated - do not modify.
 */
package sy_soapserver;


/**
 * A document containing one sendmsg(@urn:SY_SoapServer) element.
 *
 * This is a complex type.
 */
public interface SendmsgDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SendmsgDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4262BC6C064011807FED7385999847C2").resolveHandle("sendmsg081edoctype");
    
    /**
     * Gets the "sendmsg" element
     */
    sy_soapserver.SendmsgDocument.Sendmsg getSendmsg();
    
    /**
     * Sets the "sendmsg" element
     */
    void setSendmsg(sy_soapserver.SendmsgDocument.Sendmsg sendmsg);
    
    /**
     * Appends and returns a new empty "sendmsg" element
     */
    sy_soapserver.SendmsgDocument.Sendmsg addNewSendmsg();
    
    /**
     * An XML sendmsg(@urn:SY_SoapServer).
     *
     * This is a complex type.
     */
    public interface Sendmsg extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Sendmsg.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4262BC6C064011807FED7385999847C2").resolveHandle("sendmsg9603elemtype");
        
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
         * Gets the "msg" element
         */
        java.lang.String getMsg();
        
        /**
         * Gets (as xml) the "msg" element
         */
        org.apache.xmlbeans.XmlString xgetMsg();
        
        /**
         * Tests for nil "msg" element
         */
        boolean isNilMsg();
        
        /**
         * Sets the "msg" element
         */
        void setMsg(java.lang.String msg);
        
        /**
         * Sets (as xml) the "msg" element
         */
        void xsetMsg(org.apache.xmlbeans.XmlString msg);
        
        /**
         * Nils the "msg" element
         */
        void setNilMsg();
        
        /**
         * Gets the "phone" element
         */
        java.lang.String getPhone();
        
        /**
         * Gets (as xml) the "phone" element
         */
        org.apache.xmlbeans.XmlString xgetPhone();
        
        /**
         * Tests for nil "phone" element
         */
        boolean isNilPhone();
        
        /**
         * Sets the "phone" element
         */
        void setPhone(java.lang.String phone);
        
        /**
         * Sets (as xml) the "phone" element
         */
        void xsetPhone(org.apache.xmlbeans.XmlString phone);
        
        /**
         * Nils the "phone" element
         */
        void setNilPhone();
        
        /**
         * Gets the "sendtime" element
         */
        java.lang.String getSendtime();
        
        /**
         * Gets (as xml) the "sendtime" element
         */
        org.apache.xmlbeans.XmlString xgetSendtime();
        
        /**
         * Tests for nil "sendtime" element
         */
        boolean isNilSendtime();
        
        /**
         * Sets the "sendtime" element
         */
        void setSendtime(java.lang.String sendtime);
        
        /**
         * Sets (as xml) the "sendtime" element
         */
        void xsetSendtime(org.apache.xmlbeans.XmlString sendtime);
        
        /**
         * Nils the "sendtime" element
         */
        void setNilSendtime();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static sy_soapserver.SendmsgDocument.Sendmsg newInstance() {
              return (sy_soapserver.SendmsgDocument.Sendmsg) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static sy_soapserver.SendmsgDocument.Sendmsg newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (sy_soapserver.SendmsgDocument.Sendmsg) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static sy_soapserver.SendmsgDocument newInstance() {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static sy_soapserver.SendmsgDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static sy_soapserver.SendmsgDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static sy_soapserver.SendmsgDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static sy_soapserver.SendmsgDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static sy_soapserver.SendmsgDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static sy_soapserver.SendmsgDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static sy_soapserver.SendmsgDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static sy_soapserver.SendmsgDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (sy_soapserver.SendmsgDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
