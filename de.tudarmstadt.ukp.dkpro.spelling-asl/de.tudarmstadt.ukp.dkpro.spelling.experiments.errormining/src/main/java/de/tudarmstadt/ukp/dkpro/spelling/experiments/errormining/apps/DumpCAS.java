/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.spelling.experiments.errormining.apps;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.WikipediaReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.WikipediaRevisionPairReader;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class DumpCAS
{

    public static void main(String[] args)
        throws Exception
    {
//        dumpDe();
        dumpEn();
    }
    
    private static void dumpDe() throws Exception {
        CollectionReader readerDe = createReader(
                WikipediaRevisionPairReader.class,
                WikipediaReaderBase.PARAM_HOST,       "bender.ukp.informatik.tu-darmstadt.de",
                WikipediaReaderBase.PARAM_DB,         "wiki_de_20100813_rev",
                WikipediaReaderBase.PARAM_USER,       "",
                WikipediaReaderBase.PARAM_PASSWORD,   "",
                WikipediaReaderBase.PARAM_LANGUAGE,   Language.german.name(),
                WikipediaRevisionPairReader.PARAM_MAX_CHANGE, 3
        );
        
        AnalysisEngineDescription dumperDe = createEngineDescription(
                BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, "target/de/bincas"
        );

        SimplePipeline.runPipeline(
                readerDe,
                dumperDe
        );
    }

    private static void dumpEn() throws Exception {
        CollectionReader readerEn = createReader(
                WikipediaRevisionPairReader.class,
                WikipediaReaderBase.PARAM_HOST,       "bender.ukp.informatik.tu-darmstadt.de",
                WikipediaReaderBase.PARAM_DB,         "wikiapi_simple_20090119",
                WikipediaReaderBase.PARAM_USER,       "",
                WikipediaReaderBase.PARAM_PASSWORD,   "",
                WikipediaReaderBase.PARAM_LANGUAGE,   Language.english.name(),
                WikipediaRevisionPairReader.PARAM_MAX_CHANGE, 3
        );
        
        AnalysisEngineDescription dumperEn = createEngineDescription(
                BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, "target/en/bincas"
        );

        SimplePipeline.runPipeline(
                readerEn,
                dumperEn
        );
    }
}
