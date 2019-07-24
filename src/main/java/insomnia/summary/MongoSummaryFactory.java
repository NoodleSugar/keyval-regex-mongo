package insomnia.summary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import insomnia.summary.Summary.Builder.BuilderException;
import insomnia.json.JsonParser;
import insomnia.json.JsonWriter;

public final class MongoSummaryFactory implements ISummaryFactory
{
	private static MongoSummaryFactory INSTANCE = new MongoSummaryFactory();
	
	private MongoSummaryFactory()
	{}
	
	public static MongoSummaryFactory getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public Summary load(InputStream in)
	{
		JsonParser parser = new JsonParser();
		Summary summary = new Summary.Builder(null).build();
		try
		{
			summary.data = parser.readJsonStream(in);
		}
		catch(ParseException | IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return summary;
	}

	@Override
	public void save(OutputStream out, ISummary summary)
	{
		try
		{
			JsonWriter.writeJson(out, summary.getData(), true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param documents is the document set of a MongoDB collection
	 */
	@Override
	public Summary generate(Iterable<? extends Object> documents)
	{
		Summary.Builder builder = new Summary.Builder(Summary.Builder.RootType.OBJECT);
		for(Object d : documents)
		{
			try
			{
				buildingDocument(builder, (Document) d);
			}
			catch(BuilderException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private void buildingDocument(Summary.Builder builder, Document document) throws BuilderException
	{
		for(Map.Entry<String, Object> entry : document.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();

			if(value instanceof Document)
			{
				builder.addObject(key);
				buildingDocument(builder, key, (Document) value);
				builder.goBack();
			}
			else if(value instanceof List)
			{
				builder.addArray(key);
				buildingArray(builder, key, (List<Object>) value);
				builder.goBack();
			}
			else
				builder.addKey(key);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buildingDocument(Summary.Builder builder, String key, Document document) throws BuilderException
	{
		for(Map.Entry<String, Object> entry : document.entrySet())
		{
			String newKey = entry.getKey();
			Object newValue = entry.getValue();

			if(newValue instanceof Document)
			{
				builder.addObject(key);
				buildingDocument(builder, newKey, (Document) newValue);
				builder.goBack();
			}
			else if(newValue instanceof List)
			{
				builder.addArray(key);
				buildingArray(builder, newKey, (List<Object>) newValue);
				builder.goBack();
			}
			else
				builder.addKey(newKey);
		}
	}

	private void buildingArray(Summary.Builder builder, String key, List<Object> array) throws BuilderException
	{
		for(Object element : array)
		{
			if(element instanceof Document)
			{
				builder.addObject();
				buildingDocument(builder, (Document) element);
				builder.goBack();
			}
		}
	}
}
