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
package de.tudarmstadt.ukp.dkpro.spelling.experiments.eacl2012;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.spelling.experiments.data.util.DataUtil;

public class TestDatasets {

	@Ignore
    @Test
    public void test() throws IOException {
        int i=0;
        for (Entry<String, String> datasetEntry : DataUtil.getAllDatasets("classpath*:/datasets", new String[]{"txt"}).entrySet()) {
            System.out.println(datasetEntry.getValue() + " --- " + datasetEntry.getKey());
            i++;
        }
        
        assertEquals(i, 8);
    }
}
