package de.tudarmstadt.ukp.dkpro.spelling.experiments.hoo2011;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.jazzy.SpellChecker;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;
import de.tudarmstadt.ukp.dkpro.spelling.detector.knowledge.LexCohesionDetector;
import de.tudarmstadt.ukp.dkpro.spelling.detector.ngram.TrigramProbabilityDetector;
import de.tudarmstadt.ukp.dkpro.spelling.experiments.core.RWSECandidateAnnotator;
import de.tudarmstadt.ukp.dkpro.spelling.experiments.core.RWSECandidateFilter;
import de.tudarmstadt.ukp.dkpro.spelling.experiments.core.SpellingErrorCombinator;
import de.tudarmstadt.ukp.dkpro.spelling.experiments.core.SpellingErrorCombinator.CombinationStrategy;
import de.tudarmstadt.ukp.dkpro.spelling.experiments.core.SpellingPipeline_Base;
import dkpro.similarity.algorithms.lsr.uima.path.JiangConrathRelatednessResource;
import dkpro.similarity.algorithms.lsr.uima.path.LinRelatednessResource;
import dkpro.similarity.algorithms.vsm.uima.VectorIndexSourceRelatednessResource;

public class RunHOO2011Experiments
	extends SpellingPipeline_Base
{
    private static final String LANG = "en";
    private final static String TEAM_ID = "UD";

    protected static boolean WRITE_EDITS = true;

    private static final float ESA_THRESHOLD = 0.1f;
    private static final float PATH_THRESHOLD = 0.5f;
    private static final float ALPHA = 0.7f;

	private final static String MIN_NGRAM_LEVEL = "1";
	private final static String MAX_NGRAM_LEVEL = "3";
    
	private final static int MIN_LENGTH = 2;
    
	private final static String NGRAM_MODEL_WEB1T = "en";
//	private final static String NGRAM_MODEL_WIKI  = "wiki_en";
	private final static String NGRAM_MODEL_ACL   = "ACL_ANTHOLOGY";
	
	private enum SRMeasures {
	    JiangConrath,
	    Lin,
	    EsaWkt,
	    EsaWP,
	    EsaWN
	}
	
// ----------------
// There have been some problem with the encoding of the test files which resulted in wrong offsets.
// Make sure that either the data is read in UTF8 mode or the UTF8 characters in the data are replaced preserving the offsets.
// ----------------
    
    private final static String ENCODING = "ISO-8859-1";

	// ISO - no problem reading as ISO
//    public final static String DATASET =  "classpath:/hoo2011/develop/Raw/";
    
    // containing some UTF8 characters - read as ISO to preserve offsets
//    public final static String DATASET =  "classpath:/hoo2011/test/Raw_old/";				
  
    // ISO
    public final static String DATASET = "classpath:/hoo2011/test/Raw/";  
//    public final static String DATASET = "src/test/resources/test_data/Raw/";  
    
  
    
//    private final static String GOLD_PATH = "classpath:/hoo2011/develop/Gold/";
    private final static String GOLD_PATH =   "classpath:/hoo2011/test/Gold/";
//    private final static String GOLD_PATH =   "src/test/resources/test_data/Gold/";

    private final static String OUTPUT_PATH     = "target/hoo_output/";
    private final static String EXTRACTION_PATH = "target/hoo_extraction/";
    
    public static void main(String[] args) throws Exception
    {

        int runId = 0;
        // do nothing
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                      NoOpAnnotator.class
                )
        );
        
        // Jazzy
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        SpellChecker.class,
                        SpellChecker.PARAM_MODEL_LOCATION, vocabularyMap.get(LANG)
                )
        );

        // HB2005
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, PATH_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.Lin)
                 )
        );

        // HB2005
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, PATH_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.JiangConrath)
                 )
        );

        // HB2005
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWP)
                 )
        );

        // HB2005
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWkt)
                 )
        );

        // HB2005
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWN)
                 )
        );

        //  WOHB2008
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_WEB1T)
                )
        );
        
        //  WOHB2008
        runSingleDetectorPipeline(
                runId++,
                DATASET,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_ACL)
                )
        );
        
        //// ------ combinations -------------

        runMultipleDetectorPipeline(
                runId++,
                DATASET,
                CombinationStrategy.join,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        SpellChecker.class,
                        SpellChecker.PARAM_MODEL_LOCATION, vocabularyMap.get(LANG)
                ),
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, PATH_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.JiangConrath)
                ),
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWN)
                 ),
                 createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_WEB1T)
                ),
                createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_ACL)
                )
        );

        runMultipleDetectorPipeline(
                runId++,
                DATASET,
                CombinationStrategy.join,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, PATH_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.JiangConrath)
                ),
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWN)
                 ),
                 createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_WEB1T)
                ),
                createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_ACL)
                )
        );

        runMultipleDetectorPipeline(
                runId++,
                DATASET,
                CombinationStrategy.onlyKeepMultiple,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        SpellChecker.class,
                        SpellChecker.PARAM_MODEL_LOCATION, vocabularyMap.get(LANG)
                ),
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWN)
                 ),
                 createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_ACL)
                )
        );
        
        runMultipleDetectorPipeline(
                runId++,
                DATASET,
                CombinationStrategy.onlyKeepMultiple,
                CANDIDATE_TOKEN,
                createEngineDescription(
                        LexCohesionDetector.class,
                        LexCohesionDetector.PARAM_LANGUAGE_CODE, LANG,
                        LexCohesionDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        LexCohesionDetector.PARAM_THRESHOLD, ESA_THRESHOLD,
                        LexCohesionDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        LexCohesionDetector.SR_RESOURCE, getExternalResource(SRMeasures.EsaWN)
                 ),
                 createEngineDescription(
                        TrigramProbabilityDetector.class,
                        TrigramProbabilityDetector.PARAM_LANGUAGE_CODE, LANG,
                        TrigramProbabilityDetector.PARAM_VOCABULARY, vocabularyMap.get(LANG),
                        TrigramProbabilityDetector.PARAM_MIN_LENGTH, MIN_LENGTH,
                        TrigramProbabilityDetector.PARAM_ALPHA, ALPHA,
                        TrigramProbabilityDetector.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_ACL)
                )
        );

    }

    protected static CollectionReader getReader(String dataset) throws ResourceInitializationException {
        return createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, dataset,
                TextReader.PARAM_ENCODING, ENCODING,
                TextReader.PARAM_PATTERNS, new String[] { TextReader.INCLUDE_PREFIX + "*.txt" }
        );
    }

    protected static AnalysisEngineDescription getEvaluator(Integer runId) throws ResourceInitializationException {
        return createEngineDescription(
                HOO2011Evaluator.class,
                HOO2011Evaluator.PARAM_OUTPUT_PATH, OUTPUT_PATH + "/",
                HOO2011Evaluator.PARAM_EXTRACTION_PATH, EXTRACTION_PATH + "/",
                HOO2011Evaluator.PARAM_GOLD_PATH, GOLD_PATH,
                HOO2011Evaluator.PARAM_TEAM_ID, TEAM_ID,
                HOO2011Evaluator.PARAM_RUN_ID, runId.toString(),
                HOO2011Evaluator.PARAM_WRITE_EDITS, WRITE_EDITS
        );
    }
    
	private static ExternalResourceDescription getNGramProvider(String ngramModel) throws IOException {
		File context = DkproContext.getContext().getWorkspace("web1t");
		System.out.println(new File(context, ngramModel).getAbsolutePath());
		
		return createExternalResourceDescription(
			Web1TFrequencyCountResource.class,
			Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, MIN_NGRAM_LEVEL,
			Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, MAX_NGRAM_LEVEL,
			Web1TFrequencyCountResource.PARAM_INDEX_PATH, new File(context, ngramModel).getAbsolutePath());
	}

    
    private static void runSingleDetectorPipeline(
            int runId,
            String dataset,
            String candidateType,
            AnalysisEngineDescription detector
    )
        throws UIMAException, IOException
    {

        SimplePipeline.runPipeline(
                getReader(dataset),
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(
                        TreeTaggerPosLemmaTT4J.class,
                        TreeTaggerPosLemmaTT4J.PARAM_LANGUAGE, LANG
                ),
                createEngineDescription(
                        RWSECandidateAnnotator.class,
                        RWSECandidateAnnotator.PARAM_TYPE, candidateType
                ),
                createEngineDescription(
                		RWSECandidateFilter.class,
                        RWSECandidateFilter.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_WEB1T),
                        RWSECandidateFilter.PARAM_STOPWORD_LIST, blacklistMap.get(LANG),
                        RWSECandidateFilter.PARAM_MIN_LENGTH, MIN_LENGTH
                ),
                detector,
                getEvaluator(runId)
        );
    }
    
    private static void runMultipleDetectorPipeline(
            int runId,
            String dataset,
            CombinationStrategy strategy,
            String candidateType,
            AnalysisEngineDescription ... detectors
    )
        throws UIMAException, IOException
    {
        
        SimplePipeline.runPipeline(
                getReader(dataset),
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(
                        TreeTaggerPosLemmaTT4J.class,
                        TreeTaggerPosLemmaTT4J.PARAM_LANGUAGE, LANG
                ),
                createEngineDescription(
                        RWSECandidateAnnotator.class,
                        RWSECandidateAnnotator.PARAM_TYPE, candidateType
                ),
                createEngineDescription(
                		RWSECandidateFilter.class,
                        RWSECandidateFilter.FREQUENCY_PROVIDER_RESOURCE, getNGramProvider(NGRAM_MODEL_WEB1T),
                        RWSECandidateFilter.PARAM_STOPWORD_LIST, blacklistMap.get(LANG),
                        RWSECandidateFilter.PARAM_MIN_LENGTH, MIN_LENGTH
                ),
                createEngineDescription(detectors),
                createEngineDescription(
                        SpellingErrorCombinator.class,
                        SpellingErrorCombinator.PARAM_COMBINATION_STRATEGY, strategy.name()
                ),
                getEvaluator(runId)
        );
    }

    private static ExternalResourceDescription getExternalResource(SRMeasures measure)
        throws IOException
    {
        if (measure.equals(SRMeasures.JiangConrath)) {
            return createExternalResourceDescription(
                    JiangConrathRelatednessResource.class,
                    JiangConrathRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
                    JiangConrathRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
            );
        }
        else if (measure.equals(SRMeasures.Lin)) {
            return createExternalResourceDescription(
                    LinRelatednessResource.class,
                    LinRelatednessResource.PARAM_RESOURCE_NAME, "wordnet",
                    LinRelatednessResource.PARAM_RESOURCE_LANGUAGE, "en"
            );
        }
        else if (measure.equals(SRMeasures.EsaWP)) {
            return createExternalResourceDescription(
                    VectorIndexSourceRelatednessResource.class,
                    VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
                    DkproContext.getContext().getWorkspace("esaIndexesVector" + "/en/wp/").getAbsolutePath()
            );

        }
        else if (measure.equals(SRMeasures.EsaWkt)) {
            return createExternalResourceDescription(
                    VectorIndexSourceRelatednessResource.class,
                    VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
                    DkproContext.getContext().getWorkspace("esaIndexesVector" + "/en/wkt/").getAbsolutePath()
            );

        }
        else if (measure.equals(SRMeasures.EsaWN)) {
            return createExternalResourceDescription(
                    VectorIndexSourceRelatednessResource.class,
                    VectorIndexSourceRelatednessResource.PARAM_MODEL_LOCATION,
                    DkproContext.getContext().getWorkspace("esaIndexesVector" + "/en/wordnet/").getAbsolutePath()
            );
        }
        else {
            throw new IOException();
        }
    }
}