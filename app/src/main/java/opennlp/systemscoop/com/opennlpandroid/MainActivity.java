package opennlp.systemscoop.com.opennlpandroid;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class MainActivity extends AppCompatActivity {

    EditText inputEt;
    SentencesDialogFragment sentencesDialogFragment = null;
    InputStream tokenModelIn = null;
    InputStream posModelIn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEt = (EditText) findViewById(R.id.inputText);

        try {
            tokenModelIn = getAssets().open("en-token.bin");//new FileInputStream("en-token.bin");
            posModelIn = getAssets().open("en-pos-maxent.bin");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");


    }


    public void onClickButton(View view){


        try {
            String sentence = "1, Before we started we did a dummy run.\n" +
                    "2, He viciously bayoneted the straw dummy.\n" +
                    "3, No, you dummy. The other hand.";
            // tokenize the sentence
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            Tokenizer tokenizer = new TokenizerME(tokenModel);
            String tokens[] = tokenizer.tokenize(sentence);


            try {
                //AssetFileDescriptor fileDescriptor = getAssets().openFd("en-pos-maxent.bin");
                //posModelIn = fileDescriptor.createInputStream();
                POSModel posModel = new POSModel(posModelIn);
                POSTaggerME posTagger = new POSTaggerME(posModel);


            // Parts-Of-Speech Tagging
            // reading parts-of-speech model to a stream
            //posModelIn = getAssets().open("en-pos-maxent.bin");//new FileInputStream("en-pos-maxent.bin");
            // loading the parts-of-speech model from stream
            //POSModel posModel = new POSModel(posModelIn);
            // initializing the parts-of-speech tagger with model
            //POSTaggerME posTagger = new POSTaggerME(posModel);
            // Tagger tagging the tokens
            String tags[] = posTagger.tag(tokens);
            // Getting the probabilities of the tags given to the tokens
            double probs[] = posTagger.probs();

            StringBuilder result = new StringBuilder();
            System.out.println("Token\t:\tTag\t:\tProbability\n---------------------------------------------");
            for(int i=0;i<tokens.length;i++){
                System.out.println(tokens[i]+"\t:\t"+tags[i]+"\t:\t"+probs[i]);
                result.append(tokens[i]).append("\t:\t").append(tags[i]).append("\t:\t").append(probs[i]).append("\n");
            }

                inputEt.setText(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        }
        finally {
            if (tokenModelIn != null) {
                try {
                    tokenModelIn.close();
                }
                catch (IOException e) {
                }
            }
            if (posModelIn != null) {
                try {
                    posModelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    public void onClickButton1(View view) {

        long startTime = System.currentTimeMillis();
        long consumeTime = 0;

        //Loading sentence detector model
        InputStream inputStream = null;
        SentenceModel model = null;
        String[] sentences = null;
        Span[] sentencesIndex = null;
        try {
            inputStream = getAssets().open("en-sent.bin");
            model = new SentenceModel(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Instantiating the SentenceDetectorME class
        if (model != null) {
            SentenceDetectorME detector = new SentenceDetectorME(model);

            //Detecting the sentence
            sentences = detector.sentDetect(inputEt.getText().toString());
            sentencesIndex = detector.sentPosDetect(inputEt.getText().toString());

        }


        consumeTime = System.currentTimeMillis() - startTime;
        ArrayList<String> listSentences = new ArrayList<>();
        ArrayList<Span> spanArrayList = new ArrayList<>();
        if (sentences != null) {
            spanArrayList = new ArrayList<>(Arrays.asList(sentencesIndex));
        }



        FragmentManager fm = getSupportFragmentManager();
        SentencesDialogFragment sentencesDialogFragment = new SentencesDialogFragment(spanArrayList, inputEt.getText().toString(), consumeTime);

        if (fm.findFragmentByTag("fragment_answer_dialog") == null) {
            sentencesDialogFragment.show(fm, "fragment_answer_dialog");
        }

    }
}
