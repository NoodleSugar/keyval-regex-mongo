package insomnia.query;

import java.util.ArrayList;
import java.util.List;

import insomnia.summary.ISummary;

public final class MongoQueryFactory implements IQueryFactory
{
	private static MongoQueryFactory INSTANCE = null;
	private final int DEFAULT_BLOCK_SIZE = 1000;
	
	private MongoQueryFactory(){}

	public static MongoQueryFactory getInstance()
	{
		if(INSTANCE == null)
			INSTANCE = new MongoQueryFactory();
		return INSTANCE;
	}
	
	@Override
	public ArrayList<String> getQueries(List<String> paths, ISummary summary)
	{
		return getQueries(paths, DEFAULT_BLOCK_SIZE);
	}
	
	public ArrayList<String> getQueries(List<String> summaryPaths, int blockSize)
	{
		ArrayList<String> queries = new ArrayList<String>();
		StringBuffer query = new StringBuffer();

		int i = 0;
		int size = summaryPaths.size();
		while(i < size)
		{
			query.append("{$or:[{\"").append(summaryPaths.get(i++)).append("\":{$exists:true}}");
			for(int j = 1; j < blockSize && i < size; i++, j++)
			{
				query.append(",{\"").append(summaryPaths.get(i)).append("\":{$exists:true}}");
			}
			query.append("]}");
			queries.add(query.toString());
			query.setLength(0);
		}
		return queries;
	}
}
