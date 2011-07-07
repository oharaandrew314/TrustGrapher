package cu.repsystestbed.tests;

import java.text.ParseException;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ARFFTest {

	public static void testbedARFF() throws Exception
	{
		//header
		FastVector attributes = new FastVector();
		
		Attribute groupID = new Attribute("groupID");
		
		Attribute agentID = new Attribute("agentID");

		FastVector strategyAttributes = new FastVector();
		Attribute feedbackMean = new Attribute("feedbackMean");
		Attribute feedbackStdDev = new Attribute("feedbackStdDev");
		Attribute targetGroupdID = new Attribute("targetGroupID");
		strategyAttributes.addElement(feedbackMean);
		strategyAttributes.addElement(feedbackStdDev);
		strategyAttributes.addElement(targetGroupdID);
		Instances temp = new Instances("strategy", strategyAttributes, 3);
		
		Attribute strategy = new Attribute("strategy", temp);
		
		attributes.addElement(groupID);
		attributes.addElement(agentID);
		attributes.addElement(strategy);
		
		//instances
		Instances data = new Instances("mydataset", attributes, 0);
		
		double[] values = new double[data.numAttributes()];
		values[0] = 0;
		values[1] = 0;
		Instances strategyData = new Instances(data.attribute(2).relation(), 0);
		double[] values4Strategy = new double[3];
		values4Strategy[0] = 0.8;
		values4Strategy[1] = 0.1;
		values4Strategy[2] = 0;
		strategyData.add(new Instance(1.0, values4Strategy));
		values[2] = data.attribute(2).addRelation(strategyData);
		
		data.add(new Instance(1.0, values));
		System.out.println(data);
		
		
		
	}
	
	public static void testbedARFFfromFile() throws Exception
	{
		Attribute groupID = new Attribute("groupID", (FastVector) null);
		
		DataSource source = new DataSource("testMe.arff");
		Instances instances = source.getDataSet();
		//System.out.println(instances);
		System.out.println(instances.instance(0));
		
				
		Enumeration enu = instances.enumerateInstances();
		while(enu.hasMoreElements())
		{
			Instance temp = (Instance)enu.nextElement();
			for(int i=0;i<temp.numValues();i++)
			{
				System.out.println(temp.stringValue(i));
			}
		}
		//System.out.println(instances.instance(0).stringValue(2));

		
		
		
		
		
	}
	
	public static void sampleFromWeka() throws Exception
	{

		FastVector      atts;
	     FastVector      attsRel;
	     FastVector      attVals;
	     FastVector      attValsRel;
	     Instances       data;
	     Instances       dataRel;
	     double[]        vals;
	     double[]        valsRel;
	     int             i;
	 
	     // 1. set up attributes
	     atts = new FastVector();
	     // - numeric
	     atts.addElement(new Attribute("att1"));
	     // - nominal
	     attVals = new FastVector();
	     for (i = 0; i < 5; i++)
	       attVals.addElement("val" + (i+1));
	     atts.addElement(new Attribute("att2", attVals));
	     // - string
	     atts.addElement(new Attribute("att3", (FastVector) null));
	     // - date
	     atts.addElement(new Attribute("att4", "yyyy-MM-dd"));
	     // - relational
	     attsRel = new FastVector();
	     // -- numeric
	     attsRel.addElement(new Attribute("att5.1"));
	     // -- nominal
	     attValsRel = new FastVector();
	     for (i = 0; i < 5; i++)
	       attValsRel.addElement("val5." + (i+1));
	     attsRel.addElement(new Attribute("att5.2", attValsRel));
	     dataRel = new Instances("att5", attsRel, 0);
	     atts.addElement(new Attribute("att5", dataRel, 0));
	 
	     // 2. create Instances object
	     data = new Instances("MyRelation", atts, 0);
	 
	     // 3. fill with data
	     // first instance
	     vals = new double[data.numAttributes()];
	     // - numeric
	     vals[0] = Math.PI;
	     // - nominal
	     vals[1] = attVals.indexOf("val3");
	     // - string
	     vals[2] = data.attribute(2).addStringValue("This is a string!");
	     // - date
	     vals[3] = data.attribute(3).parseDate("2001-11-09");
	     // - relational
	     dataRel = new Instances(data.attribute(4).relation(), 0);
	     // -- first instance
	     valsRel = new double[2];
	     valsRel[0] = Math.PI + 1;
	     valsRel[1] = attValsRel.indexOf("val5.3");
	     dataRel.add(new Instance(1.0, valsRel));
	     // -- second instance
	     valsRel = new double[2];
	     valsRel[0] = Math.PI + 2;
	     valsRel[1] = attValsRel.indexOf("val5.2");
	     dataRel.add(new Instance(1.0, valsRel));
	     vals[4] = data.attribute(4).addRelation(dataRel);
	     // add
	     data.add(new Instance(1.0, vals));
	 
	     // second instance
	     vals = new double[data.numAttributes()];  // important: needs NEW array!
	     // - numeric
	     vals[0] = Math.E;
	     // - nominal
	     vals[1] = attVals.indexOf("val1");
	     // - string
	     //vals[2] = data.attribute(2).addStringValue("And another one!");
	     vals[2] = Instance.missingValue();
	     
	     
	     // - date
	     vals[3] = data.attribute(3).parseDate("2000-12-01");
	     // - relational
	     dataRel = new Instances(data.attribute(4).relation(), 0);
	     // -- first instance
	     valsRel = new double[2];
	     valsRel[0] = Math.E + 1;
	     valsRel[1] = attValsRel.indexOf("val5.4");
	     dataRel.add(new Instance(1.0, valsRel));
	     // -- second instance
	     valsRel = new double[2];
	     valsRel[0] = Math.E + 2;
	     valsRel[1] = attValsRel.indexOf("val5.1");
	     dataRel.add(new Instance(1.0, valsRel));
	     vals[4] = data.attribute(4).addRelation(dataRel);
	     // add
	     Instance inst = new Instance(1.0, vals);
	     inst.setMissing(2);
	     data.add(inst);
	     
	     
	 
	     // 4. output data
	     System.out.println(data);


	}
	public static void main(String[] args) throws Exception 
	{
		//sampleFromWeka();
		//testbedARFF();
		testbedARFFfromFile();
		
		//Instance.main(null);
	}

}
