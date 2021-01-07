package piero.aldinucci.apt.bookstore.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import piero.aldinucci.apt.bookstore.controller.BookstoreControllerImpl;
import piero.aldinucci.apt.bookstore.exceptions.BookstorePersistenceException;
import piero.aldinucci.apt.bookstore.model.Author;
import piero.aldinucci.apt.bookstore.model.Book;
import piero.aldinucci.apt.bookstore.service.BookstoreManager;
import piero.aldinucci.apt.bookstore.view.AuthorView;
import piero.aldinucci.apt.bookstore.view.BookView;
import piero.aldinucci.apt.bookstore.view.ComposeBookView;
import piero.aldinucci.apt.bookstore.view.factory.ViewsFactory;

@RunWith(GUITestRunner.class)
public class ControllerViewIT extends AssertJSwingJUnitTestCase{

	@Mock
	private BookstoreManager manager;
	
	private BookstoreControllerImpl controller;
	private FrameFixture window;
	private DialogFixture dialog;
	
	private List<Author> authors;
	private List<Book> books;

	private class IntegrationTestModule extends AbstractModule{
		
		@Override
		protected void configure() {
			bind(BookstoreManager.class).toInstance(manager);
			install(new FactoryModuleBuilder()
				.implement(AuthorView.class, AuthorSwingView.class)
				.implement(BookView.class, BookSwingView.class)
				.implement(ComposeBookView.class, ComposeBookSwingView.class)
				.build(ViewsFactory.class));
		}
		
		@Provides
		BookstoreControllerImpl getController(ViewsFactory viewsFactory) {
			BookstoreControllerImpl controller = new BookstoreControllerImpl(manager);
			controller.setAuthorView(viewsFactory.createAuthorView(controller));
			controller.setBookView(viewsFactory.createBookView(controller));
			controller.setComposeBookView(viewsFactory.createComposeBookView(controller));
			return controller;
		}
	}
	

	@Override
	protected void onSetUp() throws Exception {
		openMocks(this);
		Injector injector = Guice.createInjector(new IntegrationTestModule());
		
		GuiActionRunner.execute(() -> {
			controller = injector.getInstance(BookstoreControllerImpl.class);
			JFrame frame = new BookstoreSwingFrame((AuthorSwingView) controller.getAuthorView(),
					(BookSwingView) controller.getBookView());
			dialog = new DialogFixture(robot(), (ComposeBookSwingView) controller.getComposeBookView());
			window = new FrameFixture(robot(), frame);
			return frame;
		});
		
		window.show();
		allAuthorsAndBooksSetup();
	}

	
	private void allAuthorsAndBooksSetup() {
		Author author1 = new Author(1L, "Arthur", new HashSet<>());
		Author author2 = new Author(2L, "Isaac", new HashSet<>());
		Author author3 = new Author(3L, "Newton", new HashSet<>());
		Book book1 = new Book(11L, "A book", new HashSet<>());
		Book book2 = new Book(12L, "Manual", new HashSet<>());
		Book book3 = new Book(13L, "Novel", new HashSet<>());
		
		author1.getBooks().add(book3);
		author2.getBooks().add(book2);
		author2.getBooks().add(book3);
		author3.getBooks().add(book1);
		author3.getBooks().add(book2);
		book1.getAuthors().add(author3);
		book2.getAuthors().add(author2);
		book2.getAuthors().add(author3);
		book3.getAuthors().add(author1);
		book3.getAuthors().add(author2);
		
		authors = Arrays.asList(author1,author2,author3);
		books = Arrays.asList(book1,book2,book3);
		
		when(manager.getAllAuthors()).thenReturn(authors);
		when(manager.getAllBooks()).thenReturn(books);
		
		GuiActionRunner.execute(() -> {
			controller.allBooks();
			controller.allAuthors();
		});
	}
	
	
	@Test
	@GUITest
	public void test_add_new_Author() {
		when(manager.newAuthor(isA(Author.class))).thenAnswer(invocation -> {
			Author author = invocation.getArgument(0, Author.class);
			author.setId(4L);
			return author;
		});
		
		window.tabbedPane().selectTab("Authors");
		window.textBox().enterText("Test a1");
		window.button(JButtonMatcher.withText("Add")).click();
		
		Author author = new Author(4L, "Test a1", new HashSet<>());
		assertThat(window.list().item(3).value()).isEqualTo(author.toString());
	}

	@Test
	@GUITest
	public void test_compose_new_Book() {
		when(manager.newBook(isA(Book.class))).thenAnswer(invocation -> {
			Book book = invocation.getArgument(0, Book.class);
			book.setId(14L);
			return book;
		});

		window.tabbedPane().selectTab("Books");
		window.button(JButtonMatcher.withText("New Book")).click();

		dialog.textBox("titleTextField").enterText("Comic");
		dialog.list("AvailableAuthors").selectItem(1);
		dialog.button(JButtonMatcher.withText("<")).click();
		dialog.button(JButtonMatcher.withText("OK")).click();

		Book composedBook = new Book(14L, "Comic", new HashSet<>());
		assertThat(window.list().item(3).value()).isEqualTo(composedBook.toString() + "; Authors: Isaac");
	}
	
	@Test
	@GUITest
	public void test_delete_author_success() {
		window.tabbedPane().selectTab("Authors");
		window.list().selectItem(0);
		window.button("DeleteAuthor").click();
		
		assertThat(window.list().valueAt(0)).isEqualTo(authors.get(1).toString());
		assertThat(window.list().valueAt(1)).isEqualTo(authors.get(2).toString());
	}
	
	@Test
	@GUITest
	public void test_delete_author_error() {
		doThrow(new BookstorePersistenceException()).when(manager).delete(isA(Author.class));
		
		window.tabbedPane().selectTab("Authors");
		window.list().selectItem(0);
		window.button("DeleteAuthor").click();
		
		window.list().requireItemCount(3);
		assertThat(window.label("ErrorLabel").text()).isNotBlank();
	}
	
	@Test
	@GUITest
	public void test_delete_book_success() {
		window.tabbedPane().selectTab("Books");
		window.list().selectItem(1);
		window.button("DeleteBook").click();
		
		assertThat(window.list().valueAt(0)).isEqualTo(books.get(0).toString()+
				"; Authors: Newton");
		assertThat(window.list().valueAt(1)).isEqualTo(books.get(2).toString()+
				"; Authors: Arthur - Isaac");
	}

	@Test
	@GUITest
	public void test_delete_book_error() {
		doThrow(new BookstorePersistenceException()).when(manager).delete(isA(Book.class));
		
		window.tabbedPane().selectTab("Books");
		window.list().selectItem(0);
		window.button("DeleteBook").click();
		
		window.list().requireItemCount(3);
		assertThat(window.label("ErrorLabel").text()).isNotBlank();
	}
}
