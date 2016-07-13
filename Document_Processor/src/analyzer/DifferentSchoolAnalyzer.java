package analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import service.svm_predict;
import service.svm_scale;
import service.svm_train;
import util.Handle_LibSVM_Input;
import common.FeatureMap;
import component.DocumentTFIDFComputation;
import filter.Stop_Words;

public class DifferentSchoolAnalyzer {
	private String[] schools = {"NUPT","NUAA","NUST","SEU"};
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
					title_detail_list.add(new String(input1[3]));			
					title_detail_list.add(new String(input1[6]));
					result.add(title_detail_list);
					label.add(input1[1]);
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
	public List<String> addVocab(List<List<String>> NUPT_DataSet)
	{
		List<String> vocab = new ArrayList<String>();
		Iterator<List<String>> it = NUPT_DataSet.iterator();
		while(it.hasNext())
		{
			List<String> data = it.next();
			vocab.addAll(data);
		}
		return vocab;
	}
	
	public Map<Integer,List<List<String>>> splitItem(List<List<String>> NUPT_DataSet,List<String> NUPT_Label)
	{
		Map<Integer,List<List<String>>> item = new HashMap<Integer,List<List<String>>>();
		List<List<String>> dev = new ArrayList<List<String>>();
		List<List<String>> test = new ArrayList<List<String>>();
		List<List<String>> support = new ArrayList<List<String>>();
		List<List<String>> other = new ArrayList<List<String>>();
		for(int i=0;i<NUPT_Label.size();i++)
		{
			 switch(Integer.parseInt(NUPT_Label.get(i)))
			{
			 case 1:
				 dev.add(NUPT_DataSet.get(i));
				 continue;
			 case 2:
				 test.add(NUPT_DataSet.get(i));
				 continue;
			 case 3:
				 support.add(NUPT_DataSet.get(i));
				 continue;
			 case 4:other.add(NUPT_DataSet.get(i));
			 	 continue;
			}
		}
		item.put(1, dev);
		item.put(2,test);
		item.put(3,support);
		item.put(4, other);
		return item;
	}
	
	public Map<String,Object> show_parameters()
	{
		Analyzer dsa = new Analyzer();
		List<List> NUPT = dsa.loadData("D:\\Data_Classification\\svm\\train\\NUPT_dataSet.txt");
		MManalyzer mm = new MManalyzer();
		List<List<String>> NUPT_Word = mm.handleDataSet(NUPT.get(0));
		List<String> NUPT_Label = NUPT.get(1);
		Stop_Words sw = new Stop_Words();
		Set<String> stopword = sw.loadData("resources\\����ͣ�ôʱ�.txt");
		Iterator it = NUPT_Word.iterator();
		List<List<String>> NUPT_DataSet = new ArrayList<List<String>>();
		while(it.hasNext())
		{
			List<String> handle_word= sw.del_stopword(stopword, (List<String>)it.next());
			NUPT_DataSet.add(handle_word);
		}
		Map<Integer,List<List<String>>> item = dsa.splitItem(NUPT_DataSet, NUPT_Label);
		Set<String> vocab = new HashSet<String>(dsa.addVocab(NUPT_DataSet));
	
		//������һ��javabean���ڴ洢ֵ
		FeatureMap fm = new FeatureMap();
		fm.setItem(item);
		fm.setVocab(vocab);
		fm.feature_vocab(2.0);//�����ʾѡ��chiֵ���ڵ���2����Ϊ�������������Ը���ʵ�ʵ�������������
		Map<String,Integer> feature = fm.getFeature();
		System.out.println(feature.size());
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("vocab", vocab);
		parameters.put("feature",feature);
		return parameters;
	}
		
	public static void run(String[] args)
	{
		for(int i=0;i<4;i++)
		{			
			DifferentSchoolAnalyzer dsa = new DifferentSchoolAnalyzer();
			String schoolname = dsa.schools[i];
			/*
			 * ��ȡѵ�������ļ�Ĭ�ϴ����Ŀ¼D:\Data_Classification\svm\train�£������淶Ϊ:NUPT_DataSet.txt
			 * 
			*/
			List<List> NUPT = dsa.loadData("D:\\Data_Classification\\svm\\train\\"+schoolname+"_DataSet.txt");
			MManalyzer mm = new MManalyzer();
			List<List<String>> NUPT_Word = mm.handleDataSet(NUPT.get(0));
			List<String> NUPT_Label = NUPT.get(1);
			Stop_Words sw = new Stop_Words();
			Set<String> stopword = sw.loadData("resources\\����ͣ�ôʱ�.txt");
			Iterator it = NUPT_Word.iterator();
			List<List<String>> NUPT_DataSet = new ArrayList<List<String>>();
			while(it.hasNext())
			{
				List<String> handle_word= sw.del_stopword(stopword, (List<String>)it.next());
				NUPT_DataSet.add(handle_word);
			}
			Map<Integer,List<List<String>>> item = dsa.splitItem(NUPT_DataSet, NUPT_Label);
			Set<String> vocab = new HashSet<String>(dsa.addVocab(NUPT_DataSet));
			System.out.println(NUPT_DataSet.size());
			System.out.println(item.get(1).size());
			System.out.println(item.get(2).size());
			System.out.println(item.get(3).size());
			System.out.println(item.get(4).size());
			System.out.println(vocab.size());	
			//������һ��JavaBean���ڴ洢ֵ
			FeatureMap fm = new FeatureMap();
			fm.setItem(item);
			fm.setVocab(vocab);
			/*
			 * �����ʾѡ��chiֵ���ڵ���2����Ϊ�������������Ը���ʵ�ʵ�������������
			 * chi��ֵ�ɵ�һ������args[0]�ṩ
			*/
			double chi = Double.parseDouble(args[0]);
			fm.feature_vocab(chi);
			Map<String,Integer> feature = fm.getFeature();
			System.out.println(feature.size());
			//����ÿ���ĵ��е�TF-IDF��ֵ
			DocumentTFIDFComputation tfidf = new DocumentTFIDFComputation(item, vocab, feature);
			Map<Integer,List<Map<Integer,Double>>> document_tfidf = tfidf.compute();
			//�����ݽ������򣬻���libsvm�淶�������ʽ
			String input_filepath = "D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TrainDataSet.txt";
			//String input_filepath = "C:\\Users\\xinchen\\Desktop\\SVM_TrainDataSet.txt";
			Handle_LibSVM_Input hli = new Handle_LibSVM_Input(document_tfidf,input_filepath);
			hli.loadData();
			//��SVM_TrainDataSet.txt�������ѡһ���ֵ����ݣ�������������,��������ѡ��0~260���ڵĹ�30������������в���
			try {
				String testData = "D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TestDataSet.txt";
				File file3 = new File(input_filepath);
				File file4 = new File(testData);
				FileReader fr3 = new FileReader(file3.getAbsolutePath());
				FileWriter fw4 = new FileWriter(file4.getAbsolutePath());
				BufferedReader br3 = new BufferedReader(fr3);
				BufferedWriter bw4 = new BufferedWriter(fw4);
				int index = 0;
				String LineTxt = null;
				int total_num = 260;
				Set test_random = new HashSet();
				for(int j=0;j<30;j++)
				{
					test_random.add((int)(total_num*Math.random()));
				}
				while((LineTxt=br3.readLine())!=null)
				{	
					if(test_random.contains(index))
					{
						bw4.write(LineTxt);
						bw4.write("\r\n");
					}
					index += 1;
				}
				br3.close();
				bw4.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//����LibSVM��JAVA�ӿ�
			try {
				System.out.println("-------"+schoolname+"--SVM���п�ʼ-------");
				//1.ͳһѵ�����ݵĳ߶�
				String[] scaleArgs = {"-l","-1", "-u","1","-s","D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TrainDataSet.txt.scale","D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TrainDataSet.txt"};
				svm_scale.main(scaleArgs,"D:\\Data_Classification\\svm\\util\\"+schoolname+"_Scale_SVM_TrainDataSet.txt");  
				//2.ͳһ�������ݵĳ߶�
				String[] scaletestArgs = {"-l","-1", "-u","1","-s","D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TestDataSet.txt.scale","D:\\Data_Classification\\svm\\util\\"+schoolname+"_SVM_TestDataSet.txt"};
				svm_scale.main(scaletestArgs,"D:\\Data_Classification\\svm\\util\\"+schoolname+"_Scale_SVM_TestDataSet.txt"); 			
				//3.��ͳһ�������ѵ����ģ�ͣ�����ΪXX.model�ļ�
				String[] trainArgs = {"D:\\Data_Classification\\svm\\util\\"+schoolname+"_Scale_SVM_TrainDataSet.txt"};//directory of training file
				svm_train.main(trainArgs);
				//4.��ͳһ��Ĳ������ݣ���ģ�ͽ���Ԥ�⣬�������out_r.txt�ļ�
				String[] testArgs = {"D:\\Data_Classification\\svm\\util\\"+schoolname+"_Scale_SVM_TestDataSet.txt", "D:\\Data_Classification\\svm\\util\\"+schoolname+"_Scale_SVM_TrainDataSet.txt.model", "D:\\Data_Classification\\svm\\util\\"+schoolname+"_out_r.txt"};//directory of test file, model file, result file
				Double accuracy = svm_predict.main(testArgs); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
