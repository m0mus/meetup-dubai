package io.helidon.examples.quickstart.mp;

import javax.persistence.*;
import java.util.Objects;

@Access(AccessType.FIELD)
@Entity(name = "Greeting")
@Table(name = "GREETING")
public class Greeting {

    @Id
    @Column(name = "FIRSTPART", insertable = true, nullable = false, updatable = false)
    private String firstPart;

    @Basic(optional = false)
    @Column(name = "SECONDPART", insertable = true, nullable = false, updatable = true)
    private String secondPart;

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
     * @param firstPart the first part of the greeting; must not be
     * {@code null}
     *
     * @param secondPart the second part of the greeting; must not be
     * {@code null}
     *
     * @exception NullPointerException if {@code firstPart} or {@code
     * secondPart} is {@code null}
     */
    public Greeting(final String firstPart, final String secondPart) {
        super();
        this.firstPart = Objects.requireNonNull(firstPart);
        this.secondPart = Objects.requireNonNull(secondPart);
    }

    /**
     * Sets the second part of this greeting.
     *
     * @param secondPart the second part of this greeting; must not be
     * {@code null}
     *
     * @exception NullPointerException if {@code secondPart} is {@code
     * null}
     */
    public void setSecondPart(final String secondPart) {
        this.secondPart = Objects.requireNonNull(secondPart);
    }

    /**
     * Returns a {@link String} representation of the second part of
     * this {@link Greeting}.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @return a non-{@code null} {@link String} representation of the
     * second part of this {@link Greeting}
     */
    @Override
    public String toString() {
        return this.secondPart;
    }

    public String secondPart() {
        return secondPart;
    }
}
