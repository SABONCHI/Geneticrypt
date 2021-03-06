package frontend

import java.util.concurrent.atomic.AtomicInteger

import com.nomachetejuggling.geneticrypt.genes.crypt.CryptSequence

import com.nomachetejuggling.geneticrypt.util.UpdateCallback
import com.google.common.base.Supplier

import com.nomachetejuggling.geneticrypt.ciphers.MonoSubstitutionCipher
import java.security.SecureRandom
import com.nomachetejuggling.geneticrypt.simulators.genetic.EvolutionarySimulator
import com.nomachetejuggling.geneticrypt.simulators.genetic.ThreadedEvolutionarySimulator

class FrontendController {
    // these will be injected by Griffon
    def model
    def view

    EvolutionarySimulator<CryptSequence> simulator
    AtomicInteger generation

    // void mvcGroupInit(Map args) {
    //    // this method is called after model and view are injected
    // }

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

    /*
        Remember that actions will be called outside of the UI thread
        by default. You can change this setting of course.
        Please read chapter 9 of the Griffon Guide to know more.
       
    def action = { evt = null ->
    }
    */

    def action = { evt = null ->
        if (model.running) {
            model.running = false
            model.availableAction = "Crack!"
            simulator.requestStop()
        } else {
            model.running = true
            model.availableAction = "Stop"

            model.originalCipherText = model.cipherText

            generation = new AtomicInteger()
            simulator = new ThreadedEvolutionarySimulator<CryptSequence>(75);

            simulator.registerUpdates(new UpdateCallback<CryptSequence>() {
                @Override
                public void call(CryptSequence object) {
                    model.generation = generation.getAndIncrement()
                    model.key = object.getKey();
                    String potentialPlaintext = new MonoSubstitutionCipher(object.getKey()).decrypt(model.originalCipherText);
                    view.cipherText.setText(potentialPlaintext)
                }
            });

            simulator.simulate(new Supplier<CryptSequence>() {

                @Override
                public CryptSequence get() {
                    return new CryptSequence(model.originalCipherText, new SecureRandom());
                }
            });

        }
    }
}
