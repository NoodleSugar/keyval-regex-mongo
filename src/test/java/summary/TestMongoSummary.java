package summary;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import insomnia.query.MongoQueryFactory;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexToAutomatonConverter;
import insomnia.regex.element.IElement;
import insomnia.summary.MongoSummaryFactory;
import insomnia.summary.Summary;

@SuppressWarnings("unused")
class TestMongoSummary
{
	public static void main(String[] argv) throws Exception
	{
		String regex = "~.*~*.c";
		// Automate
		RegexParser parser = new RegexParser();
		IElement elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
		RegexAutomaton automaton = RegexToAutomatonConverter.convert(elements);

		// Résumé
		MongoSummaryFactory factory = MongoSummaryFactory.getInstance();
		Summary summary;

		// MongoDB
		MongoClient client = MongoClients.create();
		MongoDatabase database = client.getDatabase("xmark");
		MongoCollection<Document> collection = database.getCollection("temp");

		// Generation du résumé
//		summary = factory.generate(collection.find());

		// Enregistrement du résumé
//		OutputStream out = new
//		FileOutputStream("src/test/ressources/summary/testsummary.json");
//		factory.save(out, summary);
//		out.close();

		// Chargement du résumé
		InputStream in = new FileInputStream("src/test/ressources/summary/TestSummary.json");
		summary = factory.load(in);
		in.close();

		// Automate
		System.out.println(automaton);
		
		// Chemins
		ArrayList<String> paths = automaton.getPathsFromSummary(summary);
		System.out.println("Regex: " + regex);
		System.out.println("Valid paths:");
		for(String s : paths)
			System.out.println(s);

		// Requêtes
		ArrayList<String> queries = MongoQueryFactory.getInstance().getQueries(paths, summary);
		System.out.println("\nMongoDB queries:");
		for(String s : queries)
			System.out.println(s);
	}

}
