package com.example.feature_produtor.model;

public class LessonsItem {
    private final String id;
    private final String title;
    private final String description;

    private final int quantModulos;
    private final int imageResourceId; // Para o ícone ou imagem de capa

    public LessonsItem(String id, String title, String description, int imageResourceId, int quantModulos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.quantModulos = quantModulos;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResourceId() { return imageResourceId; }

    public int getQuantModulos() {
        return quantModulos;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_lessons_item, container, false);
        }
    }