package io.helidon.examples.quickstart.mp;

import javax.persistence.*;
import java.util.Objects;

@Access(AccessType.FIELD)
@Entity(name = "Greeting")
@Table(name = "GREETING")
public class Greeting {

    @Id
    @Column(name = "NAME", insertable = true, nullable = false, updatable = false)
    private String name;

    @Basic(optional = false)
    @Column(name = "GREETING", insertable = true, nullable = false, updatable = true)
    private String greeting;

    /**
     * Creates a new {@link Greeting}; required by the JPA
     * specification and for no other purpose.
     *
     * @deprecated Please use the {@link #Greeting(String,
     * String)} constructor instead.
     *
     * @see #Greeting(String, String)
     */
    @Deprecated
    protected Greeting() {
        super();
    }

    /**
     * Creates a new {@link Greeting}.
     *
     * @param name the name; primary key; must not be
     * {@code null}
     *
     * @param greeting the greeting; must not be
     * {@code null}
     *
     * @exception NullPointerException if {@code name} or {@code
     * greeting} is {@code null}
     */
    public Greeting(final String name, final String greeting) {
        super();
        this.name = Objects.requireNonNull(name);
        this.greeting = Objects.requireNonNull(greeting);
    }

    /**
     * Sets the second part of this greeting.
     *
     * @param greeting must not be
     * {@code null}
     *
     * @exception NullPointerException if {@code greeting} is {@code
     * null}
     */
    public void setGreeting(final String greeting) {
        this.greeting = Objects.requireNonNull(greeting);
    }

    /**
     * Returns a {@link String} representation of greeting {@link Greeting}.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @return a non-{@code null} {@link String} representation of greeting {@link Greeting}
     */
    @Override
    public String toString() {
        return this.greeting;
    }

    public String getGreeting() {
        return greeting;
    }
}
