package org.apache.geronimo.ews.ws4j2ee.toWs.ejb;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.geronimo.ews.ws4j2ee.context.J2EEWebServiceContext;
import org.apache.geronimo.ews.ws4j2ee.context.SEIOperation;
import org.apache.geronimo.ews.ws4j2ee.toWs.GenerationFault;
import org.apache.geronimo.ews.ws4j2ee.toWs.JavaInterfaceWriter;

/**
 * This class can be used to write the appropriate EJB Remote interface
 * class for the given port type.
 * 
 * @author Rajith Priyanga
 * @author Srinath Perera
 * @date Nov 26, 2003
 */
public class EJBRemoteWriter extends JavaInterfaceWriter {
	private String name;

	/**
	 * Constructs a EJBRemoteWriter.
	 * 
	 * @param portType The port type which contains the details.
	 * @throws GenerationFault 
	 */
	public EJBRemoteWriter(J2EEWebServiceContext context) throws GenerationFault {
		super(context, context.getMiscInfo().getEjbsei());
	}

	/**
	 * Returns the complete file name of the source code file which is
	 * generated by the writer.
	 */
	public String getFileName() {
		name = j2eewscontext.getMiscInfo().getEjbsei();
		String outdir = j2eewscontext.getMiscInfo().getOutPutPath();
		if (!outdir.endsWith("/"))
			outdir = outdir + "/";

		return outdir + "ejb/" + name.replace('.', '/') + ".java";
	}

	protected String getExtendsPart() {
		return " extends javax.ejb.EJBObject";
	}

	protected void writeAttributes() throws GenerationFault {
	}

	protected void writeMethods() throws GenerationFault {
		String parmlistStr = "";
		ArrayList operations = j2eewscontext.getMiscInfo().getSEIOperations();
		for (int i = 0; i < operations.size(); i++) {
			SEIOperation op = (SEIOperation) operations.get(i);
			String returnType = op.getReturnType();
			if(returnType == null)
				returnType = "void";
			out.write("\tpublic " + returnType + " " + op.getMethodName() + "(");

            
			Iterator pas = op.getParameterNames().iterator();
			boolean first = true;
			while (pas.hasNext()) {
				String name = (String) pas.next();
				String type = (String) op.getParameterType(name);
				if (first) {
					first = false;
					out.write(type + " " + name);
					parmlistStr = parmlistStr + name;
				} else {
					out.write("," + type + " " + name);
					parmlistStr = "," + name;
				}

			}

			out.write(") throws java.rmi.RemoteException");
			ArrayList faults = op.getFaults();
			for (int j = 0; j < faults.size(); j++) {
				out.write("," + (String) faults.get(i));
			}
			out.write(";\n");
		}
	}

}
