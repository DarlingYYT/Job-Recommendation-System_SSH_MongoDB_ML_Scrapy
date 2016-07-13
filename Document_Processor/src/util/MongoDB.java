package util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MongoDB {
	private static Mongo conn = null;
	private static DB db = null;
	private static DBCollection coll = null;
	
	public DBCollection getCollection(String host_port,String dbName,String collName) throws UnknownHostException, MongoException
	{
		this.conn = new Mongo(host_port);
		this.db = conn.getDB(dbName);
		this.coll = db.getCollection(collName);
		return coll;
	}
	
	
	public void insertOneDate(DBCollection coll,BasicDBObject document1)
	{
		BasicDBObject document = document1;
		coll.insert(document);
	}
	
	/* ִ�в��뵥�����
	 * �ⲿ���÷���
	 * BasicDBObject document = new BasicDBObject();
	 * document.put("id",1);
	 * document.put("msg","helloworld");
	 * List<DBObject> documents = new ArrayList<DBObject>();
	 * documents.add(document);
	 * ��documents���뺯��
	 * @param coll , documents
	 * @return cursor
	 */
	public void insertSeveralData(DBCollection coll , List<DBObject> documents1)
	{
		List<DBObject> documents = documents1;
		coll.insert(documents);
	}

	/* ִ�в�ѯ���
	 * �ⲿ���÷���
	 * BasicDBObject searchQuery = new BasicDBObject();
	 * searchQuery.put("id",1)
	 * ��searchQuery���뺯��
	 * @param coll serachQuery
	 * @return cursor
	 * ���DBCursor���淨��
	 * while(cursor.hasNext())
	 * {
	 * 	   System.out.println(cursor.next());
	 * }
	 */
	public DBCursor find(DBCollection coll,BasicDBObject searchQuery)
	{
		DBCursor cursor = coll.find(searchQuery);//����һ��findOne(DBObject o)  Returns a single object from this collection matching the query.
     											 //findOne(Object obj) Finds an object by its id.
		return cursor;
	}
	public DBCursor findAll(DBCollection coll)
	{
		DBCursor cursor = coll.find();//����һ��findOne(DBObject o)  Returns a single object from this collection matching the query.												 //findOne(Object obj) Finds an object by its id.
		return cursor;
	}
	
	public static long count(DBCollection coll,BasicDBObject searchQuery)
	{
		return coll.count(searchQuery);
	}
	
	public void delete(DBCollection coll,BasicDBObject document)
	{
		coll.remove(document);
	}
	
	public void update(DBCollection coll,BasicDBObject old_document,BasicDBObject new_document)
	{
		coll.update(old_document, new_document);
	}
}
