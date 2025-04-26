package by.javaguru.je.jdbc.dao;


import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {
    public boolean update(E e);
    public List<E> findAll();
    public Optional<E> findById(K id);
    public E save(E e);
    public boolean delete(K id);
}
