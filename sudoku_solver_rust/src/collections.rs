pub trait IteratorZipExt<T> {
    fn zip_every_pair(self) -> impl Iterator<Item = (T, T)>;
    fn zip_every_triple(self) -> impl Iterator<Item = (T, T, T)>;
    fn zip_every_quad(self) -> impl Iterator<Item = (T, T, T, T)>;
}

impl<T: Clone, I: Iterator<Item = T>> IteratorZipExt<T> for I {
    fn zip_every_pair(self) -> impl Iterator<Item = (T, T)> {
        let list: Vec<_> = self.collect();
        list.iter()
            .enumerate()
            .flat_map(|(first_index, first)| {
                list.iter()
                    .skip(first_index + 1)
                    .map(|second| (first.clone(), second.clone()))
            })
            .collect::<Vec<_>>()
            .into_iter()
    }

    fn zip_every_triple(self) -> impl Iterator<Item = (T, T, T)> {
        let list: Vec<_> = self.collect();
        list.iter()
            .enumerate()
            .flat_map(|(first_index, first)| {
                list.iter()
                    .enumerate()
                    .skip(first_index + 1)
                    .flat_map(|(second_index, second)| {
                        list.iter()
                            .skip(second_index + 1)
                            .map(|third| (first.clone(), second.clone(), third.clone()))
                    })
            })
            .collect::<Vec<_>>()
            .into_iter()
    }

    fn zip_every_quad(self) -> impl Iterator<Item = (T, T, T, T)> {
        let list: Vec<_> = self.collect();
        list.iter()
            .enumerate()
            .flat_map(|(first_index, first)| {
                list.iter()
                    .enumerate()
                    .skip(first_index + 1)
                    .flat_map(|(second_index, second)| {
                        list.iter().enumerate().skip(second_index + 1).flat_map(
                            |(third_index, third)| {
                                list.iter().skip(third_index + 1).map(|fourth| {
                                    (first.clone(), second.clone(), third.clone(), fourth.clone())
                                })
                            },
                        )
                    })
            })
            .collect::<Vec<_>>()
            .into_iter()
    }
}
