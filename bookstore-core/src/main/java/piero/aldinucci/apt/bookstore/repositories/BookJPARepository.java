package piero.aldinucci.apt.bookstore.repositories;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import piero.aldinucci.apt.bookstore.model.Book;

/**
 * JPA specific implementation of BookRepository
 *
 */

public class BookJPARepository implements BookRepository{

	private EntityManager entityManager;

	/**
	 * 
	 * @param entityManager the interface used to interact wit the persistence context.
	 */
	public BookJPARepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Book> findAll() {
		return entityManager.createQuery("from Book",Book.class).getResultList();
	}

	@Override
	public Optional<Book> findById(long id) {
		return Optional.ofNullable(entityManager.find(Book.class, id));
	}

	@Override
	public Book save(Book book) {
		if (book.getId() != null)
			throw new IllegalArgumentException("id of a new Book should be null");
		return entityManager.merge(book);
	}

	@Override
	public void update(Book book) {
		if (book.getId() == null)
			throw new IllegalArgumentException("Cannot update a book with null id");
		if (entityManager.find(Book.class, book.getId()) != null)
			entityManager.merge(book);
	}

	@Override
	public Optional<Book> delete(long id) {
		Book book = entityManager.find(Book.class, id);
		if (book != null)
			entityManager.remove(book);
		return Optional.ofNullable(book);
	}

}
