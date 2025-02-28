package dat.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Actor {
    @Id
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "actors", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Movie> movies = new HashSet<>();

}