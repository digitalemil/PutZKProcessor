package com.hortonworks.digitalemil.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.stream.io.StreamUtils;

@Tags({ "put", "Zookepper" })
@CapabilityDescription("This processor stores FlowFile content in Zookeeper")
public class PutZKProcessor extends AbstractProcessor {
	protected static String pmml= null;
	
	public static final PropertyDescriptor ZKCONNECTIONSTRING = new PropertyDescriptor.Builder()
    .name("ZKConStr")
    .description("Zookeeper connection string e.g. 127.0.0.1:2181,127.0.0.2:2181")
    .required(true)
    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
    .expressionLanguageSupported(true)
    .build();
	
	public static final PropertyDescriptor ZKPATH = new PropertyDescriptor.Builder()
    .name("ZKPath")
    .description("Zookeeper path where to store the file e.g. /foobar")
    .required(true)
    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
    .expressionLanguageSupported(true)
    .build();

	
	public static final PropertyDescriptor ZKNAME = new PropertyDescriptor.Builder()
    .name("ZKNodename")
    .description("Zookeeper nodename. File will be stored under /path/nodename")
    .required(true)
    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
    .expressionLanguageSupported(true)
    .build();


    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("files that were successfully stored in Zookeeper").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("files that were not successfully stroed in Zookeeper").build();

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> properties;
   
    @Override
    protected void init(final ProcessorInitializationContext context) {

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);

     // descriptors
        final List<PropertyDescriptor> supDescriptors = new ArrayList<>();
        supDescriptors.add(ZKCONNECTIONSTRING);
        supDescriptors.add(ZKPATH);
        supDescriptors.add(ZKNAME);
    
        properties = Collections.unmodifiableList(supDescriptors);
        
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }


    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context,
            final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        try {
        	// Read the contents of the FlowFile into a byte array
            final byte[] content = new byte[(int) flowFile.getSize()];
            final String connection= context.getProperty(ZKCONNECTIONSTRING).toString();
            final String path= context.getProperty(ZKPATH).toString();
            final String name= context.getProperty(ZKNAME).toString();
            
          
            session.read(flowFile, new InputStreamCallback() {
                @Override
                public void process(final InputStream in) throws IOException {
                    StreamUtils.fillBuffer(in, content, true);   
                    String newpmml= new String(content);
                    if(pmml== null || ! (pmml.equals(newpmml))){
                    	PutZK.putZK(connection, path, name, content);
                    	getLogger().info("Setting new value to: "+path+"/");
                    }
                    pmml= newpmml;
                }
            });
            session.transfer(flowFile, REL_SUCCESS);
        } catch (ProcessException ex) {
            getLogger().error("Unable to process", ex);
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}
