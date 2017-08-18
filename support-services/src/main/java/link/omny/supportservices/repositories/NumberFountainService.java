package link.omny.supportservices.repositories;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import link.omny.supportservices.model.NumberFountain;

@Service
public class NumberFountainService {

    @Autowired
    private NumberFountainRepository nfRepo;

    @Transactional(value = TxType.REQUIRED)
    public NumberFountain getNext(@NotNull String entityName) {
        NumberFountain next;
        try {
            List<NumberFountain> last = nfRepo.findByEntityName(entityName);
            if (last == null || last.size() == 0) {
                throw new NoSuchElementException(String
                        .format("Cannot find fountain for %1$s", entityName));
            } else if (last.size() > 1) {
                // just in case someone's been messing with the database
                Collections.sort(last, new Comparator<NumberFountain>() {
                    @Override
                    public int compare(NumberFountain o1, NumberFountain o2) {
                        return o1.getLastUsed().compareTo(o2.getLastUsed());
                    }
                });
                next = last.get(0);
                for (int i = 1; i < last.size(); i++) {
                    nfRepo.delete(last.get(i));
                }
            } else {
                next = last.get(0);
            }
            next.increment();
            return nfRepo.save(next);
        } catch (NoSuchElementException e) {
            return initFountain(entityName);
        }
    }

    protected NumberFountain initFountain(String entityName) {
        NumberFountain numberFountain = new NumberFountain(entityName);
        return nfRepo.save(numberFountain);
    }

}
