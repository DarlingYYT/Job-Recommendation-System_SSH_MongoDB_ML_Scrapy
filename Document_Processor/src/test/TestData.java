package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import service.svm_predict;
import service.svm_scale;
import service.svm_train;
import util.Handle_LibSVM_Input;
import util.MongoDB;
import analyzer.Analyzer;
import analyzer.MManalyzer;
import common.FeatureMap;
import component.DocumentTFIDFComputation;
import filter.Stop_Words;

public class TestData {
	private String[] schools = {"NUPT","NUAA","NUST","SEU"};
	private String[] tec_classfication = {"TEC","NO_TEC"};
	private String[] jobs_classfication = {"DEV","TEST","IT","OTHER"};
	private String[] bayes_school_classfication = {"D:\\train\\bayes\\Tec_NUPT.txt","D:\\train\\bayes\\Tec_NUAA.txt",
													"D:\\train\\bayes\\Tec_NUST.txt","D:\\train\\bayes\\Tec_SEU.txt"};
	public List<List> loadData(String filepath)
	{
		List<List> ls = new ArrayList<List>(); 
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> label = new ArrayList<String>(); 
		try {
			File file = new File(filepath);
			if(file.isFile() && file.exists())
			{
				InputStreamReader read = new InputStreamReader(new FileInputStream(file),"UTF-8");
				BufferedReader br = new BufferedReader(read);
				String lineTxt = null;
				while((lineTxt = br.readLine()) != null)
				{
					List<String> title_detail_list = new ArrayList<String>();
					String[] input1 = lineTxt.split("\t");
					title_detail_list.add(new String(input1[1]));			
					title_detail_list.add(new String(input1[3]));
					result.add(title_detail_list);
					label.add("1");
				}
				read.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ls.add(result);
		ls.add(label);
		return ls;		
	}
	//�����ݽ��з��࣬����Map���������Ӧ�Ľ��
	//��result�ļ��ж�����Ӧ�������ȡ��src�ļ��ж�Ӧ���У�Ȼ��洢��Map�У�����
	public Map<String,List<String>> splitTxt(String result,String src)
	{
		File result_file = new File(result);
		File src_file = new File(src);
		Map<String,List<String>> filter_testMap = new HashMap<String,List<String>>();
		List<String> dev_list = new ArrayList<String>();
		List<String> test_list = new ArrayList<String>();
		List<String> it_list = new ArrayList<String>();
		List<String> other_list = new ArrayList<String>();
		
		try {
			if((result_file.isFile() && result_file.exists())&&(src_file.isFile() && src_file.exists()))
			{
				
				InputStreamReader result_read = new InputStreamReader(new FileInputStream(result_file),"UTF-8");
				BufferedReader result_br = new BufferedReader(result_read);
				InputStreamReader src_read = new InputStreamReader(new FileInputStream(src_file),"UTF-8");
				BufferedReader src_br = new BufferedReader(src_read);

				String lineTxt = "";
				List<String> result_list = new ArrayList<String>();
				while((lineTxt = result_br.readLine()) != null)
				{
					result_list.add(lineTxt.toString().trim());
				}
				
				int index = 0;
				int i = 0; 
				while((lineTxt = src_br.readLine()) != null)
				{	
					 switch(i = Integer.parseInt(result_list.get(index).substring(0,1)))
						{
						 case 1:
							 dev_list.add(lineTxt);
							 break;
						 case 2:
							 test_list.add(lineTxt);
							 break;
						 case 3:
							 it_list.add(lineTxt);
							 break;
						 case 4:
							 other_list.add(lineTxt);
						 	 break;
						}
					 index += 1;
				}
				result_br.close();
				src_br.close();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		filter_testMap.put("DEV",dev_list);
		filter_testMap.put("TEST",test_list);
		filter_testMap.put("IT",it_list);
		filter_testMap.put("OTHER",other_list);
		return filter_testMap;
	}
	
	//�����������ݴ�����ݿ���	
	public void load2DB(String host,String dbName,String collName,List<String> documents)
	{
		try {
			MongoDB mongodb = new MongoDB();
			DBCollection coll = mongodb.getCollection(host, dbName, collName);
			//����������ݸ�ʽ����ת����documents(List<String>)---->documents1(List<DBObject>)
			//��һ���жϣ������ǰ�����List<String> documentsΪ�գ���ô����nullֵ
			if(documents.isEmpty())
			{
				Iterator<String> iter = documents.iterator();
				List<DBObject> documents1 = new ArrayList<DBObject>();
				BasicDBObject txt = new BasicDBObject();
				txt.put("intern_data", "NULL");
				txt.put("intern_info", "NULL");
				txt.put("intern_url", "NULL");
				txt.put("intern_detail", "NULL");
				documents1.add(txt);
				mongodb.insertSeveralData(coll, documents1);
				System.out.println("--- Success loaded2DB DB:"+dbName+" coll: "+collName+"---");
			}
			else
			{
				Iterator<String> iter = documents.iterator();
				List<DBObject> documents1 = new ArrayList<DBObject>();
				while(iter.hasNext())
				{
					BasicDBObject txt = new BasicDBObject();
					String lineTxt = iter.next();
					String[] args = lineTxt.split("\t");
					txt.put("intern_data", args[0]);
					txt.put("intern_info", args[1]);
					txt.put("intern_url", args[2]);
					txt.put("intern_detail", args[3]);
					documents1.add(txt);
				}				
				mongodb.insertSeveralData(coll, documents1);
				System.out.println("--- Success loaded2DB DB:"+dbName+" coll: "+collName+"---");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * ����ѧУ���η�װ
	 * ѧУ�������˳��ȡֵ:"NUPT","NUAA","NUST","SEU"
	*/
	
	
	public static void main(String[] args)
	{		
		TestData td = new TestData();
		//�������ݼ���д��
		String testData = "trainfile\\Tec_NUPT.txt";
		List<List> NUPT_TestData = td.loadData(testData);	
		Analyzer ay = new Analyzer();
		MManalyzer mm = new MManalyzer();
		List<List<String>> NUPT_Word = mm.handleDataSet(NUPT_TestData.get(0));
		List<String> NUPT_Test_Label = NUPT_TestData.get(1);
		Stop_Words sw = new Stop_Words();
		Set<String> stopword = sw.loadData("resources\\����ͣ�ôʱ�.txt");
		Iterator it = NUPT_Word.iterator();
		List<List<String>> NUPT_Test_DataSet = new ArrayList<List<String>>();
		while(it.hasNext())
		{
			List<String> handle_word= sw.del_stopword(stopword, (List<String>)it.next());
			NUPT_Test_DataSet.add(handle_word);
		}
		Map<Integer,List<List<String>>> item = ay.splitItem(NUPT_Test_DataSet, NUPT_Test_Label);
		System.out.println(NUPT_Test_DataSet.size());
		System.out.println(item.get(1).size());
		
		//��Analyzer��show_parameters()�����л�ȡ����
		Set<String> vocab = (Set<String>) ay.show_parameters("NUPT",2.0).get("vocab");
		Map<String,Integer> feature = (Map<String, Integer>) ay.show_parameters("NUPT",2.0).get("feature");
		
		//����ÿ���ĵ��е�TF-IDF��ֵ
		DocumentTFIDFComputation tfidf = new DocumentTFIDFComputation(item, vocab, feature);
		Map<Integer,List<Map<Integer,Double>>> document_tfidf = tfidf.compute();
		
		//�����ݽ������򣬻���libsvm�淶�������ʽ
		String input_filepath = "trainfile\\SVM_TestDataSet.txt";
		//String input_filepath = "C:\\Users\\xinchen\\Desktop\\SVM_TrainDataSet.txt";
		Handle_LibSVM_Input hli = new Handle_LibSVM_Input(document_tfidf,input_filepath);
		hli.loadData();		
		
		//����LibSVM��JAVA�ӿ�
		try {
			System.out.println("-------SVM���п�ʼ-------");
			//1.ͳһ�������ݵĳ߶�
			String[] scaletestArgs = {"-l","-1", "-u","1","-s","trainfile\\SVM_TestDataSet.txt.scale","trainfile\\SVM_TestDataSet.txt"};
			svm_scale.main(scaletestArgs,"trainfile\\Scale_SVM_TestDataSet.txt"); 			
			//3.��ͳһ��Ĳ������ݣ���ģ�ͽ���Ԥ�⣬�������out_r.txt�ļ�
			String[] testArgs = {"trainfile\\Scale_SVM_TestDataSet.txt", "trainfile\\NUPT_Scale_SVM_TrainDataSet.txt.model", "trainfile\\out_r.txt"};//directory of test file, model file, result file
			Double accuracy = svm_predict.main(testArgs); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//����splitTxt������ɲ������ݵķ���
		Map<String,List<String>> filter_testMap = td.splitTxt("trainfile\\out_r.txt", testData);		
		/*
		 * �������ݿ⽫���ݴ�����ݿ���
		   MongoĬ�϶˿ںſ��Բ���ֵ
		     ���ݿ������淶:	
					���ݿ����ƣ���ǰ����YY-MM-DD
					�������ƣ�ѧУ+����+����
		*/
		//�������ڸ�ʽ
        Calendar now = Calendar.getInstance();
        for(int i=0;i<4;i++)
        {
        	td.load2DB("10.128.4.134", now.get(Calendar.YEAR)+"_"+(now.get(Calendar.MONTH) + 1)+"_"+now.get(Calendar.DAY_OF_MONTH), 
        			td.schools[0]+"_"+td.tec_classfication[0]+"_"+td.jobs_classfication[i], 
        			 filter_testMap.get(td.jobs_classfication[i]));
        }
	}
}
