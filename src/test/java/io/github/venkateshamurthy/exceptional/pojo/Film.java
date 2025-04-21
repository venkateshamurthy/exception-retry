package io.github.venkateshamurthy.exceptional.pojo;

import lombok.Builder;
import lombok.Getter;

/**
 * A test pojo representing film
 * @param title of the film
 * @param director of the film
 * @param releaseYear of the film
 */
@Builder
public record Film(@Getter String title, @Getter String director,@Getter int releaseYear) {
    private static Film f = new Film("Guns Of Navarone", "J Lee Thomson", 1961);

    /**
     * A main method
     * @param args to be passed
     */
    public static void main (String[] args) {
        System.out.println(f.toString());
        System.out.println(f.getTitle());
        System.out.println(f.title());
        var whereEaglesDare = Film.builder().title("Where the Eagles Dare")
                .releaseYear(1968).director("Brian G Hutton")
                .build();
        System.out.println(whereEaglesDare);
    }
}