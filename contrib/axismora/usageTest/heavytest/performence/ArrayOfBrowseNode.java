package heavytest.performence;
/**
 * <p>This class is genarated by the tool WSDL2Ws.
 * It take care of the serialization and the desirialization of
 * the parameter types.
 * except for the serialize(), desirialize() methods it it a bean class.
 * We are specially concerned about the alogorithem used in the desirialize method.
 * If you have any suggestions to improve, your comments are welcome.</p>
 */ 
public class ArrayOfBrowseNode  implements org.apache.axismora.encoding.InOutParameter{
	public ArrayOfBrowseNode(){
	}

	private heavytest.performence.BrowseNode[] param;
	public heavytest.performence.BrowseNode[] getParam(){
		return this.param;
	}
	public void setParam(heavytest.performence.BrowseNode[] param){
		this.param = param;
	}

	public org.apache.axismora.encoding.InParameter desierialize(org.apache.axismora.MessageContext msgdata)throws org.apache.axis.AxisFault{
		java.util.Vector obj = new java.util.Vector();
		javax.xml.namespace.QName arrayname = null;
		if(org.apache.axismora.wsdl2ws.java.ParmWriter.tag == null)
			org.apache.axismora.wsdl2ws.java.ParmWriter.tag = msgdata.getTag();

		while(org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getType() != org.xmlpull.v1.XmlPullParser.START_TAG) {
			if (org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getType() == org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
				throw new org.apache.axis.AxisFault("end of the document");
			}
			org.apache.axismora.wsdl2ws.java.ParmWriter.tag = msgdata.getTag();
		}
		arrayname = new javax.xml.namespace.QName(org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getUri(),org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getLocalpart());
		org.apache.axismora.wsdl2ws.java.ParmWriter.tag = msgdata.getTag();

		//if the type of the next tag is end tag that means the content of element is null
		if(org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getType() == org.xmlpull.v1.XmlPullParser.END_TAG)
			return null;

		while(!((org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getType() == org.xmlpull.v1.XmlPullParser.END_TAG) && 
					 arrayname.equals(new javax.xml.namespace.QName(org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getUri(),org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getLocalpart())))){
			if(org.apache.axismora.wsdl2ws.java.ParmWriter.tag.getType() != org.xmlpull.v1.XmlPullParser.START_TAG){
				org.apache.axismora.wsdl2ws.java.ParmWriter.tag = msgdata.getTag();
				continue;
			}
			heavytest.performence.BrowseNode  item =(new heavytest.performence.BrowseNode());
			item.desierialize(msgdata);
			obj.add(item);
			org.apache.axismora.wsdl2ws.java.ParmWriter.tag = msgdata.getTag();
		}

		int length = obj.size();
		heavytest.performence.BrowseNode[] arr = new heavytest.performence.BrowseNode[length];
		for(int i = 0;i<length;i++)
			arr[i] = (heavytest.performence.BrowseNode)obj.get(i);
		 this.param = arr;
		return this;

	}

	public void serialize(org.apache.axis.encoding.SerializationContext context)throws java.io.IOException{
		org.apache.axismora.wsdl2ws.java.ParmWriter.tagWritten = false;
		for (int i = 0; i < param.length; i++) {
			context.writeString("<item"+i+ "  xsi:type=\"ns7:BrowseNode\"  xmlns:ns7 = \"http://performence.heavytest\">");
			if(param[i]!=null){
				org.apache.axismora.wsdl2ws.java.ParmWriter.tagWritten = true;
				param[i].serialize(context);
			}
			context.writeString("</item"+i+ ">");
		}
		//name of parameter will be written by upper level
	}

}
